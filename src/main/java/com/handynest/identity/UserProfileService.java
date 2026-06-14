package com.handynest.identity;

import com.handynest.auth.session.RefreshTokenService;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.common.error.UnauthorizedBusinessException;
import com.handynest.common.publicid.PublicIdGenerator;
import com.handynest.geo.City;
import com.handynest.geo.CityRepository;
import com.handynest.geo.Country;
import com.handynest.geo.CountryRepository;
import com.handynest.geo.District;
import com.handynest.geo.DistrictRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Locale;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final UserRepository userRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;
  private final DistrictRepository districtRepository;
  private final RefreshTokenService refreshTokenService;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public UserProfileResponse me(UserDetails userDetails) {
    User user = currentUser(userDetails);
    CustomerProfile customerProfile =
        customerProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("CustomerProfile", user.getPublicId()));
    BusinessProfile businessProfile =
        businessProfileRepository.findByUserId(user.getId()).orElse(null);

    return toResponse(user, customerProfile, businessProfile);
  }

  @Transactional
  public UserProfileResponse updateMe(UserDetails userDetails, UserProfileUpdateRequest request) {
    User user = currentUser(userDetails);

    if (request.firstName() != null) {
      user.setFirstName(request.firstName().trim());
    }
    if (request.lastName() != null) {
      user.setLastName(request.lastName().trim());
    }
    if (request.phone() != null) {
      user.setPhone(blankToNull(request.phone()));
      user.setPhoneVerified(false);
    }
    if (request.accountType() != null) {
      user.setAccountType(request.accountType());
    }
    if (request.countryCode() != null) {
      user.setCountry(findCountry(request.countryCode()));
    }
    if (request.cityId() != null) {
      City city = findCity(request.cityId());
      user.setCity(city);
      user.setCountry(city.getCountry());
    }
    if (request.districtId() != null) {
      District district = findDistrict(request.districtId());
      user.setDistrict(district);
      user.setCity(district.getCity());
      user.setCountry(district.getCity().getCountry());
    }
    if (request.preferredServiceRadiusKm() != null) {
      user.setPreferredServiceRadiusKm(request.preferredServiceRadiusKm());
    }

    BusinessProfile businessProfile =
        businessProfileRepository.findByUserId(user.getId()).orElse(null);
    if (request.businessProfile() != null) {
      if (businessProfile == null) {
        businessProfile = new BusinessProfile(user);
      }
      applyBusinessProfile(businessProfile, request.businessProfile());
      businessProfile = businessProfileRepository.save(businessProfile);
      user.setAccountType(AccountType.BUSINESS);
    }

    CustomerProfile customerProfile =
        customerProfileRepository
            .findByUserId(user.getId())
            .orElseGet(() -> customerProfileRepository.save(new CustomerProfile(user)));

    return toResponse(user, customerProfile, businessProfile);
  }

  @Transactional
  public void deleteMe(UserDetails userDetails, HttpServletRequest servletRequest) {
    User user = currentUser(userDetails);

    refreshTokenService.revokeAll(user, servletRequest);

    user.setDeleted(true);
    user.setStatus(UserStatus.DELETED);
    user.setDeletedAt(Instant.now());
    user.setPassword(passwordEncoder.encode(PublicIdGenerator.defaultGenerator().newUlid()));
    user.setFirstName("Deleted");
    user.setLastName("User");
    user.setPhone(null);
    user.setPhoneVerified(false);
    user.setEmailVerified(false);
    user.setLogo(null);
    user.setAvatarAttachmentId(null);
    user.setCountry(null);
    user.setCity(null);
    user.setDistrict(null);
    user.setPreferredServiceRadiusKm(null);
  }

  public User currentUser(UserDetails userDetails) {
    if (userDetails == null) {
      throw new UnauthorizedBusinessException("Authentication required");
    }
    User user =
        userRepository
            .findByEmail(normalizeEmail(userDetails.getUsername()))
            .orElseThrow(() -> new UnauthorizedBusinessException("Authentication required"));
    if (!UserAccountState.isSessionAllowed(user)) {
      throw new UnauthorizedBusinessException("Authentication required");
    }
    return user;
  }

  private void applyBusinessProfile(
      BusinessProfile businessProfile, BusinessProfileRequest request) {
    businessProfile.setCompanyName(request.companyName().trim());
    businessProfile.setBin(blankToNull(request.bin()));
    businessProfile.setLegalAddress(blankToNull(request.legalAddress()));
    businessProfile.setBillingEmail(blankToNull(request.billingEmail()));
    businessProfile.setContactPersonName(blankToNull(request.contactPersonName()));
    businessProfile.setContactPersonPhone(blankToNull(request.contactPersonPhone()));
  }

  private UserProfileResponse toResponse(
      User user, CustomerProfile customerProfile, BusinessProfile businessProfile) {
    return new UserProfileResponse(
        user.getPublicId(),
        user.getEmail(),
        user.getPhone(),
        user.getFirstName(),
        user.getLastName(),
        user.isEmailVerified(),
        user.isPhoneVerified(),
        user.getStatus().name(),
        user.getAccountType().name(),
        user.getRoles().stream()
            .map(Enum::name)
            .collect(TreeSet::new, TreeSet::add, TreeSet::addAll),
        user.getCountry() == null ? null : user.getCountry().getCode(),
        user.getCountry() == null ? null : user.getCountry().getNameRu(),
        user.getCity() == null ? null : user.getCity().getPublicId(),
        user.getCity() == null ? null : user.getCity().getNameRu(),
        user.getDistrict() == null ? null : user.getDistrict().getPublicId(),
        user.getDistrict() == null ? null : user.getDistrict().getNameRu(),
        user.getPreferredServiceRadiusKm(),
        user.getCustomerRiskScore(),
        new CustomerProfileResponse(
            customerProfile.getRatingAverage(),
            customerProfile.getRatingCount(),
            customerProfile.getCompletedOrdersCount(),
            customerProfile.getCanceledOrdersCount(),
            customerProfile.getDisputeCount(),
            customerProfile.getNoShowCount()),
        businessProfile == null
            ? null
            : new BusinessProfileResponse(
                businessProfile.getCompanyName(),
                businessProfile.getBin(),
                businessProfile.getLegalAddress(),
                businessProfile.getBillingEmail(),
                businessProfile.getContactPersonName(),
                businessProfile.getContactPersonPhone(),
                businessProfile.getVerificationStatus().name(),
                businessProfile.getRejectionReason()));
  }

  private Country findCountry(String countryCode) {
    return countryRepository
        .findByCodeIgnoreCase(countryCode)
        .orElseThrow(() -> new ResourceNotFoundException("Country", countryCode));
  }

  private City findCity(String cityPublicId) {
    return cityRepository
        .findByPublicId(cityPublicId)
        .orElseThrow(() -> new ResourceNotFoundException("City", cityPublicId));
  }

  private District findDistrict(String districtPublicId) {
    return districtRepository
        .findByPublicId(districtPublicId)
        .orElseThrow(() -> new ResourceNotFoundException("District", districtPublicId));
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}

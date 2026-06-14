package com.handynest.platform;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, Long> {

  Optional<PlatformSetting> findByKey(String key);
}

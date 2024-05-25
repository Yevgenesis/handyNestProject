package codezilla.handynestproject.testData;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.model.entity.Address;

public class AddressTestData {
    /**
     * AddressDto
     */
    public static final AddressDto TEST_ADDRESS_DTO1 = new AddressDto(
            "123 Unter den Linden", "Berlin", "10117", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO2 = new AddressDto(
            "456 Königsallee", "Düsseldorf", "40212", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO3 = new AddressDto(
            "789 Karl-Liebknecht-Strasse", "Leipzig", "04109", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO4 = new AddressDto(
            "321 Maximilianstrasse", "Munich", "80539", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO5 = new AddressDto(
            "555 Friedrichstrasse", "Berlin", "10117", "Germany");

    /**
     * test data Address
     */

    public static final Address TEST_ADDRESS1 = new Address(
            1L, "123 Unter den Linden", "Berlin", "10117", "Germany");

    public static final Address TEST_ADDRESS2 = new Address(
            2L, "456 Königsallee", "Düsseldorf", "40212", "Germany");

    public static final Address TEST_ADDRESS3 = new Address(
            3L, "789 Karl-Liebknecht-Strasse", "Leipzig", "04109", "Germany");

    public static final Address TEST_ADDRESS4 = new Address(
            4L, "321 Maximilianstrasse", "Munich", "80539", "Germany");

    public static final Address TEST_ADDRESS5 = new Address(
            5L, "555 Friedrichstrasse", "Berlin", "10117", "Germany");
}

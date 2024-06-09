package codezilla.handynestproject.testData;

import codezilla.handynestproject.dto.attachment.AttachmentDto;
import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;

public class AttachmentTestData {
    /**
     * AttachmentDto
     */
    public static final AttachmentDto TEST_ATTACHMENT_DTO1 = new AttachmentDto(
            1L,
            5L,
            "passport_1",
            "jpg",
            "attachment/img/"
    );

    public static final AttachmentRequestDto TEST_ATTACHMENT_REQUEST_DTO6 = new AttachmentRequestDto(
            5L,
            "TestName",
            "TestType",
            "TestURL.test"
    );
    public static final AttachmentDto TEST_ATTACHMENT_DTO6 = new AttachmentDto(
            6L,
            5L,
            "TestName",
            "TestType",
            "TestURL.test"
    );


}

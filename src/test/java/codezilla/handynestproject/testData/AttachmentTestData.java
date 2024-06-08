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
            "logo1",
            "jpg",
            "attachment/testAttachment1.jpg"
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

package codezilla.handynestproject.testData;

import codezilla.handynestproject.model.entity.WorkingTime;

public class WorkingTimeTestData {
    /**
     * test date WorkingTime
     */

    public static final WorkingTime TEST_WORKING_TIME1 = WorkingTime.builder()
            .id(1L).title("с 8 до 12").build();

    public static final WorkingTime TEST_WORKING_TIME2 = WorkingTime.builder()
            .id(2L).title("с 12 до 16").build();

    public static final WorkingTime TEST_WORKING_TIME3 = WorkingTime.builder()
            .id(3L).title("с 16 до 22").build();

    public static final WorkingTime TEST_WORKING_TIME4 = WorkingTime.builder()
            .id(4L).title("в любое время").build();
}

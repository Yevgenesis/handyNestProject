//package codezilla.handynestproject.model.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Builder
//@Table(name = "feedback")
//@AllArgsConstructor
//@NoArgsConstructor
//public class Feedback {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private User sender_id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private User receiver_id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Task task;
//
//}

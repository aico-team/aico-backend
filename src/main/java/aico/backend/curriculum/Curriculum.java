package aico.backend.curriculum;

import jakarta.persistence.*;

@Entity
public class Curriculum {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String curriculum;

    @Column(nullable = false)
    private String topic;

}

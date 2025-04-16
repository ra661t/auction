package com.easybid.test;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter //각각의 변수에 따로 적용할 수도 있다.
//@AllArgsConstructor //모든 변수를 사용하는 생성자를 자동완성 시켜주는 어노테이션
@Entity
public class Test {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Setter //예시
    private String user;
    private LocalDateTime date;
    private int abd;

    public Test() {
        this.date = LocalDateTime.now();
        this.abd = 0;
    }
}

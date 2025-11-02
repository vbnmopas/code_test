package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

/**
 *문제: 생성자 오버로딩이 과도하게 존재하여 코드 가독성이 떨어지고 유지보수가 어려움.
 *      JSON 역직렬화 시 어떤 생성자를 사용할지 판단하지 못해 매핑 오류 발생 가능.
 *원인: DTO에 불필요하게 여러 생성자를 정의하여, 직렬화/역직렬화 과정에서 혼란이 발생함.
 *개선안: 모든 생성자 오버로딩 제거 후 기본 생성자(@NoArgsConstructor)만 유지.
 *       객체 생성 시에는 Builder 또는 단일 생성자(@AllArgsConstructor)를 활용.
 */
@Getter
@Setter
public class UpdateProductRequest {
    private Long id;
    private String category;
    private String name;

    public UpdateProductRequest(Long id) {
        this.id = id;
    }

    public UpdateProductRequest(Long id, String category) {
        this.id = id;
        this.category = category;
    }

    public UpdateProductRequest(Long id, String category, String name) {
        this.id = id;
        this.category = category;
        this.name = name;
    }
}


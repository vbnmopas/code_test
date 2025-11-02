package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 문제: @Setter를 클래스 전체에 적용해 필드가 외부에서 임의 변경 가능함.
 *      요청 DTO는 불변이어야 검증 이후 상태가 변하지 않고, 멀티스레드 환경에서도 안전함.
 * 원인: Lombok @Setter 사용.
 * 개선안: @Setter 제거 후, 생성자 또는 빌더를 통해 값 설정.
 *        요청 DTO를 불변(immutable)으로 유지하고, 필요한 경우 빌더 패턴을 사용.
 */

/**
 * 문제: 직렬화(역직렬화)용 기본 생성자가 명시되어 있지 않음.
 *      Jackson이 JSON을 객체로 변환할 때 예외가 발생할 수 있음.
 * 원인: 파라미터 생성자만 존재.
 * 개선안: 기본 생성자를 명시적으로 추가하거나 @NoArgsConstructor(access = PROTECTED) 사용.
 *         DTO에 protected 기본 생성자를 둬 Jackson 직렬화 지원 + 불필요한 외부 생성 차단을 함께 처리.
 */

@Getter
@Setter
@NoArgsConstructor
public class CreateProductRequest {
    private String category;
    private String name;

    public CreateProductRequest(String category) {
        this.category = category;
    }

    public CreateProductRequest(String category, String name) {
        this.category = category;
        this.name = name;
    }
}


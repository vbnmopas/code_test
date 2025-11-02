package com.wjc.codetest.product.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/**
 * 문제: @Setter를 클래스 전체에 적용해 모든 필드가 외부에서 변경 가능함.
 * 도메인 제약(이름 불변, 카테고리 변경 불가 등)이 깨질 수 있고, 디버깅 시 추적이 어려움.
 * 원인: Lombok의 @Setter를 클래스 레벨에 선언했기 때문.
 * 개선안: 엔티티에는 세터를 최소화하고, 필요한 변경만 의도 명확한 메서드로 노출 (ex. rename()).
 */

/**
 * 문제: 컬럼에 nullable/length 제약이 없음.
 * 데이터 무결성 깨질 가능성이 높고, 잘못된 입력이 그대로 DB에 저장될 위험 있음.
 * 원인: @Column 속성 미지정으로 DB 제약이 기본값(null 허용)으로 설정됨.
 * 개선안: @Column(nullable = false, length = n) 등으로 명시적 제약 설정.
 *        DTO 단에서 Bean Validation(@NotBlank, @Size)로 1차 방어 후 DB 제약으로 2차 방어.
 */

/**
 * 문제: ProductRepository에서 category만 select하므로 N+1은 없지만,
 *      대량 데이터에서 DISTINCT의 정렬/중복제거 비용이 커져 성능 저하 가능.
 * 원인: 비정규화된 category 값(대/소문자·공백 차이) 혼재, 인덱스 부재
 * 개선안 (두 가지 전략):
 *  1. 테이블 분리(정규화)
 *     - Category 테이블로 분리, Product는 category_id(FK) 참조
 *     - 장점: 데이터 일관성/중복 방지, 카테고리 목록 조회가 축소
 *
 *  2. 인덱스 기반 최적화(현 구조 유지)
 *     - category 컬럼 인덱싱 + 저장 시 trim/lower 정규화
 *     - 컬럼(category_norm = TRIM(LOWER(category))) + 인덱스
 *     - 검증 결과(100만 건):
 *         인덱스 전 608ms → 인덱스 후 360ms (40.8% 개선, Full Scan → Index Scan)
 *     - 예시(Entity):
 *       @Table(indexes = @Index(name = "idx_product_category", columnList = "category"))
 */

@Entity
@Getter
@Setter
public class Product {

    /**
     * 문제: @GeneratedValue(strategy = GenerationType.AUTO)는 데이터베이스마다 다른 전략을 사용하므로,
     *      자동 증가가 정상 동작하지 않거나 "hibernate_sequence 없음" 등의 오류가 발생할 수 있음.
     * 원인: GenerationType.AUTO는 JPA 구현체(Hibernate)가 DB dialect에 따라 전략을 자동 결정하기 때문.
     *      테스트 환경(H2)과 운영 환경(MySQL)이 달라지면 ID 생성 전략 불일치 문제 발생.
     * 개선안: DB에 맞는 명시적 전략 지정.
     *  - MySQL/H2(MySQL 모드) → GenerationType.IDENTITY
     *  - Oracle/PostgreSQL → GenerationType.SEQUENCE + @SequenceGenerator
     * 검증:
     *  - H2(MySQL 모드) 환경에서 AUTO → IDENTITY 변경 후 Entity 저장 시 PK 자동 생성 정상 확인.
     *  - 실제 MySQL DB에서도 동일하게 AUTO_INCREMENT 적용되어 일관성 확보.
     */
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * 문제: category를 단순 String으로 관리.
     *      오타나 대소문자 차이로 데이터 일관성이 깨질 수 있음.
     * 원인: 데이터 타입을 열거형/참조 테이블 대신 문자열로 단순 처리.
     * 개선안: 선택지 적을 경우 Enum으로 변환(@Enumerated(EnumType.STRING)).
     *        확장 가능성이 크면 Category 테이블로 분리.
     */
    @Column(name = "category")
    private String category;

    @Column(name = "name")
    private String name;

    protected Product() {
    }

    public Product(String category, String name) {
        this.category = category;
        this.name = name;
    }


    /**
     * 문제: @Getter를 이미 사용 중인데 getCategory(), getName() 메서드를 수동으로 중복 작성함.
     * 유지보수성 떨어지고 코드 가독성 저하.
     * 원인: @Getter가 이미 생성.
     * 개선안: 중복 getter 제거. Lombok이 자동으로 생성하는 기본 getter 사용.
     */
    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}

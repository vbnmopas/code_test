package com.wjc.codetest.product.repository;

import com.wjc.codetest.product.model.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 문제: Repository 메서드의 파라미터명이 실제 필드(category)와 불일치함.
     *      서비스 계층에서는 dto.getCategory()를 사용하지만, Repository는 name으로 정의되어 혼란을 유발함.
     * 원인: 메서드 시그니처 작성 시 필드명과 다른 파라미터명을 사용함.
     * 개선안: 엔티티 필드명과 동일하게 파라미터명을 category로 변경하여 가독성과 일관성 확보.
     *        예) Page<Product> findAllByCategory(String category, Pageable pageable);
     */
    Page<Product> findAllByCategory(String name, Pageable pageable);

    /**
     * 문제: @Query("SELECT DISTINCT p.category FROM Product p")는 단순하지만,
     *      대소문자/공백이 다른 중복 카테고리를 구분하지 못할 수 있음.
     * 원인: 카테고리 데이터가 정규화되지 않은 상태에서 DISTINCT 사용.
     * 개선안: 저장 시 category를 trim()/대문자 변환 등으로 정규화하거나,
     *        쿼리에서 LOWER/TRIM을 적용해 중복 제거를 일관성 있게 수행.
     *        Enum 또는 별도 Category 테이블로 분리하는 것도 고려.
     *
     * 예시:
     *  @Query("SELECT DISTINCT TRIM(LOWER(p.category)) FROM Product p")
     */
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
}

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
     *      데이터가 수만 건 이상일 경우 전체 테이블 스캔이 발생하여 성능 저하 가능성이 높음.
     *      특히 LOWER/TRIM 같은 함수가 추가되면 인덱스를 활용하지 못해 정렬·중복 제거 비용이 커짐.
     * 원인: category 컬럼이 비정규화된 상태(중복, 대소문자 차이 등)
     *      DISTINCT 쿼리가 전체 데이터를 대상으로 수행됨.
     * 개선안: Category를 별도 테이블로 분리하고 Product는 FK(category_id)로 참조.
     *     → 카테고리 조회 시 수십~수백 건 수준으로 조회 범위 축소.
     *     → UNIQUE 제약으로 중복·오타 방지.
     *      단기적으로는 category 컬럼에 인덱스 생성 및 저장 시 정규화(trim/lower)
     */
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
}

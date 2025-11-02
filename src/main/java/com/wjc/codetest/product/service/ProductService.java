package com.wjc.codetest.product.service;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * [예외 처리 전반]
 * 문제: 서비스 계층에서 RuntimeException을 직접 던지거나 예외를 아예 처리하지 않아,
 *      클라이언트가 단순 500 내부 서버 오류만 받게 됨.
 *      GlobalExceptionHandler가 존재하나 패키지 범위가 한정되어 대부분의 예외가 처리되지 않음.
 * 원인: Custom 예외 클래스와 전역 예외 처리가 제대로 구성되지 않음.
 * 개선안: 공통 예외 구조를 정의하고, 각 도메인별로 명확한 예외 타입을 생성해 상황별 HTTP 상태 코드로 매핑.
 *         예) 존재하지 않는 리소스 → 404, 잘못된 입력 → 400, 서버 내부 오류 → 500.
 *         @RestControllerAdvice를 루트 패키지 기준으로 전역 적용하고, 일관된 ErrorResponse(또는 ProblemDetail) 구조로 반환.
 */

 /**
 * [트랜잭션 처리 누락]
 * 문제: 서비스 계층의 create(), update(), deleteById() 등 데이터 변경 메서드에 @Transactional이 없음.
 *      예외 발생 시 롤백이 되지 않아 데이터 정합성이 깨질 위험이 있음.
 *      조회 메서드도 readOnly=false로 열려 불필요한 쓰기 컨텍스트가 생성됨.
 * 원인: 트랜잭션 경계 설정이 고려되지 않음.
 * 개선안: 쓰기 작업에는 @Transactional, 읽기 작업에는 @Transactional(readOnly = true) 명시.
 *        트랜잭션 경계를 서비스 계층에 두어 비즈니스 로직 단위로 원자성을 보장.
 */


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(CreateProductRequest dto) {
        Product product = new Product(dto.getCategory(), dto.getName());
        return productRepository.save(product);
    }

    public Product getProductById(Long productId) {

        /**
         * 문제: Optional에서 isPresent() → get()으로 꺼내는 패턴.
         *      코드가 장황하고 NPE 방어가 약함.
         * 원인: Optional 사용 패턴 미숙.
         * 개선안: orElseThrow() 사용으로 한 줄로 단순화.
         *        Optional은 orElseThrow() 또는 map/filter 패턴으로 처리.
         *
         * 전:
         *  if (!productOptional.isPresent()) throw new RuntimeException();
         *  return productOptional.get();
         * 후:
         *  return productRepository.findById(productId)
         *         .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
         */
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new RuntimeException("product not found");
        }
        return productOptional.get();
    }

    /**
     * 문제: 서비스에서 setCategory(), setName()을 통해 엔티티 필드를 직접 변경하고 있음.
     *      도메인 규칙(이름 수정 불가, 카테고리 변경 제한 등)이 존재할 경우 쉽게 깨질 수 있음.
     * 원인: 모든 필드를 Setter로 공개하여 도메인 보호가 불가능함.
     * 개선안: Product 엔티티에 의미 있는 변경 메서드(예: changeName(), changeCategory())를 정의하고
     *         내부에서 유효성 검증을 수행하도록 수정.
     *         도메인 객체가 스스로 상태를 검증하게 하여 데이터 무결성을 보장함.
     */
    public Product update(UpdateProductRequest dto) {
        Product product = getProductById(dto.getId());
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        Product updatedProduct = productRepository.save(product);
        return updatedProduct;

    }

    public void deleteById(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    /**
     * 문제: 정렬 기준(Sort)이 코드 내부에 하드코딩되어 확장성이 떨어짐.
     *      정렬 기준이 고정되어 있으면, 다른 컬럼(name, price 등)으로 정렬 시 코드 수정이 필요함.
     * 원인: Sort.by(Sort.Direction.ASC, "category")로 정렬 기준을 직접 명시함.
     * 개선안: 요청 DTO에 sortBy, direction 필드를 추가하여 정렬 기준을 동적으로 처리.
     *        기본값을 설정해 예외 상황에서도 안정적으로 동작하도록 설계.
     */
    public Page<Product> getListByCategory(GetProductListRequest dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.ASC, "category"));
        return productRepository.findAllByCategory(dto.getCategory(), pageRequest);
    }

    public List<String> getUniqueCategories() {
        return productRepository.findDistinctCategories();
    }
}
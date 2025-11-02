package com.wjc.codetest.product.controller;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.model.response.ProductListResponse;
import com.wjc.codetest.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

 /**
 * [엔티티 직접 반환 문제]
 * 문제: Product 엔티티를 API 응답으로 그대로 노출하고 있음.
 *      엔티티에는 내부 구현 정보, 지연 로딩 프록시, 불필요한 필드가 포함되어 직렬화 오류, 보안 이슈, JPA Lazy 로딩 문제 (N+1 발생 가능)로 이어질 수 있음.
 * 원인: 요청/응답 DTO를 분리하지 않고 엔티티를 그대로 ResponseEntity에 담아 반환.
 * 개선안: ProductResponse, ProductListResponse 등 전용 DTO를 만들어 필요한 필드만 노출.
 *         서비스 계층에서 엔티티 → DTO로 변환(map)하여 컨트롤러는 DTO만 반환하도록 설계.
 *         영속성 관리용, DTO는 API 통신용으로 분리하여 유지보수성과 보안성을 높임.
 */

 /**
 * [입력값 검증(Valid) 부재]
 * 문제: CreateProductRequest, UpdateProductRequest getProductListByCategory 등 요청 DTO에 유효성 검증이 없음.
 *      잘못된 값(null, 빈 문자열, 음수 등)이 그대로 DB 트랜잭션으로 넘어가 데이터 무결성을 해칠 수 있음.
 * 원인: Bean Validation(@NotBlank, @NotNull 등)과 @Valid 사용 누락.
 * 개선안: 각 요청 DTO에 검증 애너테이션 추가 후, 컨트롤러 메서드 파라미터에 @Valid 명시.
 *         전역 예외 처리에서 MethodArgumentNotValidException을 캐치해 400 응답으로 변환.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /**
     * 문제: URL 패턴이 /get/product/by/{productId} 형태로, REST 관점에서 부자연스러움.
     *      'get', 'by' 같은 동사는 REST URI에서는 불필요함.
     * 원인: 메서드 명과 경로 명을 혼동하여 작성.
     * 개선안: 단수 리소스 조회는 /products/{productId} 형태로 단순화.
     *       REST 규약상 /products/{id}, /users/{id}처럼 리소스 중심으로 설계.
     */
    @GetMapping(value = "/get/product/by/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable(name = "productId") Long productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @PostMapping(value = "/create/product")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest dto) {
        Product product = productService.create(dto);
        return ResponseEntity.ok(product);
    }

    /**
    * 문제: 삭제 요청을 POST 메서드로 처리하고 있음.
    *      REST 관점에서 리소스 삭제는 DELETE 메서드를 사용하는 것이 표준이며,
    *      POST는 일반적으로 생성에 사용됨.
    * 원인: HTTP 메서드의 의미적 구분을 고려하지 않음.
    * 개선안: @DeleteMapping("/products/{productId}")로 변경.
    *         DELETE 메서드를 사용하고, 클라이언트가 이를 지원하지 않으면 POST + 명시적 의도(delete)로 처리하는 식으로 예외적으로만 사용함.
    */

    /**
     * 문제: 삭제 성공 시 항상 ResponseEntity.ok(true)를 반환.
     *      불필요한 true 값보다는 상태 코드 204 No Content로 충분히 의도를 표현할 수 있음.
     * 원인: 성공 여부를 명시적으로 전달하려는 의도지만, REST 규약상 중복 정보가 됨.
     * 개선안: ResponseEntity.noContent().build()로 반환하여 204 상태 전달.
     *        삭제 성공 시 보통 본문 없이 204 응답을 사용.
     */
    @PostMapping(value = "/delete/product/{productId}")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable(name = "productId") Long productId) {
        productService.deleteById(productId);
        return ResponseEntity.ok(true);
    }

    /**
     * 문제: HTTP 메서드로 POST를 사용하고 있음.
     *      REST 관점에서 리소스 수정은 PUT(전체 수정) 또는 PATCH(부분 수정)가 더 적합함.
     * 원인: 단순하게 POST를 CRUD 전용으로 사용한 패턴.
     * 개선안: @PutMapping("/products/{id}") 또는 @PatchMapping으로 변경하여 의미를 명확히 표현.
     */

    /**
     * 문제: id를 요청 본문(RequestBody)에서 받도록 설계되어 RESTful 원칙에 어긋남.
     *      id는 수정 대상 리소스를 식별하는 값으로, URL 경로(PathVariable)로 전달하는 것이 표준에 맞음.
     * 원인: UpdateProductRequest DTO에 id 필드를 포함시켜 본문으로 전달받도록 설계함.
     * 개선안: id 필드를 DTO에서 제거하고, 컨트롤러 메서드에서 @PathVariable로 별도 처리.
     *         요청 본문은 변경 가능한 데이터(category, name 등)만 포함하도록 설계.
     */
    @PostMapping(value = "/update/product")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProductRequest dto) {
        Product product = productService.update(dto);
        return ResponseEntity.ok(product);
    }


    /**
     * 문제: 목록 조회에 POST를 사용하고 Body DTO로 파라미터를 받음. 캐시/프록시/북마크에 불리하고 REST 의미와도 안 맞음.
     * 원인: 요청 DTO를 쓰려다 보니 POST로 고정.
     * 개선안: GET /products 로 전환하고 @RequestParam (또는 Pageable)로 category, page, size를 받기.
     * 예시:
     *  @GetMapping("/products")
     *  public ResponseEntity<ProductListResponse> getProducts(
     *        @RequestParam String category,
     *        @PageableDefault(size = 10) Pageable pageable)
     */

    /**
     * 문제: 컨트롤러에 DTO 매핑 로직이 섞임.
     * 원인: 변환 책임이 컨트롤러에 있음.
     * 개선안: repository→service에서 Page<Product>를 Page<ProductResponse>로 map 한 뒤
     *         ProductListResponse.of(Page<ProductResponse>)로 캡슐화 반환. 컨트롤러는 변환 관여 X.
     */
    @PostMapping(value = "/product/list")
    public ResponseEntity<ProductListResponse> getProductListByCategory(@RequestBody GetProductListRequest dto) {
        Page<Product> productList = productService.getListByCategory(dto);
        return ResponseEntity.ok(new ProductListResponse(productList.getContent(), productList.getTotalPages(), productList.getTotalElements(), productList.getNumber()));
    }

    @GetMapping(value = "/product/category/list")
    public ResponseEntity<List<String>> getProductListByCategory() {
        List<String> uniqueCategories = productService.getUniqueCategories();
        return ResponseEntity.ok(uniqueCategories);
    }
}
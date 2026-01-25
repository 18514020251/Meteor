package com.meteor.admin.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.meteor.admin.domain.dto.MerchantApplyDTO;
import com.meteor.admin.domain.dto.MerchantApplyQueryDTO;
import com.meteor.admin.domain.enums.MerchantApplyStatusEnum;
import com.meteor.admin.domain.es.MerchantApplyES;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.common.domain.PageResult;
import com.meteor.common.utils.EsResultUtils;
import com.meteor.common.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 商家申请ES服务
 * @author Programmer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantApplyESService {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * 根据查询条件搜索商家申请
     */
    public PageResult<MerchantApplyES> searchPage(MerchantApplyQueryDTO query)
            throws IOException {

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("merchant_apply")
                .query(q -> q.bool(buildBoolQuery(query)))
                .from(PageUtils.offset(query.getPageNum(), query.getPageSize()))
                .size(query.getPageSize())
        );

        SearchResponse<MerchantApplyES> response =
                elasticsearchClient.search(searchRequest, MerchantApplyES.class);

        List<MerchantApplyES> list = EsResultUtils.parseHits(response);

        long total = response.hits().total() == null
                ? 0
                : response.hits().total().value();

        return PageResult.of(
                list,
                total,
                query.getPageNum(),
                query.getPageSize()
        );
    }

    /*
    *  检查索引是否存在
    * */
    public void initIndex() throws IOException {
        boolean exists = elasticsearchClient.indices()
                .exists(e -> e.index("merchant_apply"))
                .value();

        if (!exists) {
            CreateIndexResponse createResponse = elasticsearchClient.indices()
                    .create(c -> c.index("merchant_apply"));
        }
    }

    /*
     *  构建查询条件
     * */
    private BoolQuery buildBoolQuery(MerchantApplyQueryDTO query) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (query.getUserId() != null) {
            boolQuery.must(q -> q.term(t -> t
                    .field("userId")
                    .value(query.getUserId())
            ));
        }

        if (query.getShopName() != null && !query.getShopName().isBlank()) {
            boolQuery.must(q -> q.match(m -> m
                    .field("shopName")
                    .query(query.getShopName())
            ));
        }

        if (query.getStatus() != null) {
            boolQuery.must(q -> q.term(t -> t
                    .field("status")
                    .value(query.getStatus())
            ));
        }

        return boolQuery.build();
    }


    /**
     * 将ES实体转换为DTO
     */
    public MerchantApplyDTO toDTO(MerchantApplyES esEntity) {
        MerchantApply entity = new MerchantApply();
        entity.setId(esEntity.getId());
        entity.setApplyId(esEntity.getApplyId());
        entity.setUserId(esEntity.getUserId());
        entity.setShopName(esEntity.getShopName());
        entity.setApplyReason(esEntity.getApplyReason());
        entity.setStatus(MerchantApplyStatusEnum.fromCode(esEntity.getStatus()));
        entity.setRejectReason(esEntity.getRejectReason());
        entity.setReviewedBy(esEntity.getReviewedBy());
        entity.setReviewedTime(esEntity.getReviewedTime());
        entity.setCreateTime(esEntity.getCreateTime());
        return MerchantApplyDTO.fromEntity(entity);
    }

}

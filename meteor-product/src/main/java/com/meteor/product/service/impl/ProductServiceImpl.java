package com.meteor.product.service.impl;

import com.meteor.product.domain.entity.Product;
import com.meteor.product.mapper.ProductMapper;
import com.meteor.product.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-01
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

}

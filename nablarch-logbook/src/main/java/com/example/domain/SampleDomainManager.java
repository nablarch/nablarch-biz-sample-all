package com.example.domain;

import nablarch.core.validation.ee.DomainManager;

/**
 * {@link DomainManager} の実装クラス。
 * <p>
 * ドメインを定義したBeanクラスを返却する。
 * </p>
 */
public class SampleDomainManager implements DomainManager<SampleDomainBean> {
    @Override
    public Class<SampleDomainBean> getDomainBean() {
        // ドメインBeanのClassオブジェクトを返す。
        return SampleDomainBean.class;
    }
}

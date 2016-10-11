package please.change.me.core.validation.convertor;

import java.math.BigDecimal;

import nablarch.core.validation.PropertyName;


public class TestTarget {
    private BigDecimal param;

    @PropertyName(messageId = "PROP0001")
    public void setParam(BigDecimal param) {
        this.param = param;
    }

    public BigDecimal getParam() {
        return param;
    }
}

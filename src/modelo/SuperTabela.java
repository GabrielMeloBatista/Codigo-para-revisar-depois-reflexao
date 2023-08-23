package modelo;

import anotacao.Campo;
import utils.ReflexaoTabela;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class SuperTabela<TypePK> {
    @SuppressWarnings("unchecked")
    public TypePK getPk() {
        return (TypePK) ReflexaoTabela.getPkValue(this);
    }

    public String getPkName() {
        return ReflexaoTabela.getPkName(this);
    }

    public void setPk(TypePK value) {
        ReflexaoTabela.setPkValue(this, value);
    }

    public String getTableName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}

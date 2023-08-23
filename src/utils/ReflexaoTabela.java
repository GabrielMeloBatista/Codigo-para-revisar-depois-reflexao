package utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import anotacao.Campo;
import anotacao.Nome;
import modelo.SuperTabela;
import modelo.Tabela;

public class ReflexaoTabela {
    private static Field getPkField(SuperTabela tab) {
        validarParametroTab(tab);
        int numPks = 0;
        Field pkField = null;
        String pkNome = null;
        // obter metadado do objeto SuperTabela
        Class<?> cls = tab.getClass();
        // obter nome dos atributos da classe
        Field[] atributos = cls.getDeclaredFields();

        // percorrer os atributos procurando pela anotacao @Campo
        for (Field attr : atributos) {
            if (attr.isAnnotationPresent(Campo.class)) {
                Campo cmp = attr.getAnnotation(Campo.class);
                if (cmp.isPk()) {
                    pkField = attr;
                    pkNome = cmp.colunaNome();
                    numPks++;
                }
            }
        }
        if (pkField == null || pkNome.isEmpty()) {
            throw new RuntimeException(
                    "Classe: " + cls.getName() + " não tem nenhum atributo anotado com @Campo(isPk=True)");
        } else if (numPks > 1) {
            throw new RuntimeException(
                    "Classe: " + cls.getName() + " tem mais de um atributo anotado com @Campo(isPk=True)");
        }
        return pkField;
    }

    public static String getPkName(SuperTabela tab) {
        Field pkField = getPkField(tab);
        Campo cmp = pkField.getAnnotation(Campo.class);
        return cmp.colunaNome();

    }

    public static Object getPkValue(SuperTabela tab) {
        Field pkField = getPkField(tab);
        String pkMethodName = "get" + getUCFirst(pkField.getName());
        return invokeGetMethod(tab, pkMethodName);
    }

    private static void validarParametroTab(SuperTabela tab) {
        if (isCamposObrigatoriosPreenchidos(tab)) {
            throw new RuntimeException("Erro algum parametro isObrigatorio não foi preenchido");
        }
        if (tab == null) {
            throw new RuntimeException("Erro Metodo ReflexaoTabela.getPkNome, deve receber um objeto não nulo");
        }
    }

    private static Object invokeGetMethod(SuperTabela tab, String pkMethodName) {
        return invokeMethod(tab, pkMethodName, null);
    }

    private static Object invokeMethod(SuperTabela tab, String pkMethodName, Object value) {
        Class<?> cls = tab.getClass();

        Method pkMethod = null;
        try {
            if (value == null) {
                pkMethod = cls.getMethod(pkMethodName);
                return pkMethod.invoke(tab);
            } else {
                pkMethod = cls.getMethod(pkMethodName, value.getClass());
                return pkMethod.invoke(tab, value);
            }

        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("A classe: " + cls.getName() + " não possui o metodo: " + pkMethodName + "(" + value.getClass().getName() + ") com visibilidade pública, ou não existe!");
        } catch (IllegalAccessException |
                 IllegalArgumentException |
                 InvocationTargetException e) {
            throw new RuntimeException("Houve um erro desconhecido na execução do método:" + cls.getName() + "." + pkMethodName, e);
        }
    }

    public static String getUCFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static void setPkValue(SuperTabela<?> tab, Object value) {
        Field pkField = getPkField(tab);
        setValue(tab, pkField, value);
    }

    public static void setValue(SuperTabela<?> tab, Field field, Object value) {
        String pkMethodName = "set" + getUCFirst(field.getName());
        invokeMethod(tab, pkMethodName, value);
    }

    public static Boolean isCamposObrigatoriosPreenchidos(SuperTabela tab) {
        Class<?> cls = tab.getClass();
        Field[] campos = cls.getDeclaredFields();

        for (Field campo : campos) {
            Campo anotacao = campo.getAnnotation(Campo.class);
            if (anotacao != null && anotacao.isObrigatorio()) {
                try {
                    String getAtributo = "get" + getUCFirst(campo.getName());
                    Object value = invokeGetMethod(tab , getAtributo);
                    if (value == null) {
                        return true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Classe: " + tab.getClass().getName() + " erro, acesso ilegal na validação de valor obrigatorio no campo:" + campo.getName());
                }

            }
        }
        return false;
    }

    private static String getTableName(SuperTabela tab) {
        Class<?> cls = tab.getClass();

        if (cls.isAnnotationPresent(Nome.class) ) {
            Nome nome = cls.getAnnotation(Nome.class);
            if (nome.getTabelaName().isEmpty()) {
                return nome.getTabelaName();
            }
        }
        return cls.getSimpleName();
    }
}

/**
 * ResultadoEvaluacionCuestionarioBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class ResultadoEvaluacionCuestionarioBPDTO  implements java.io.Serializable {
    private String claveCIFIN;

    private int codigoCuestionario;

    private int numeroAciertos;

    private RespuestaBPDTO respuestaProceso;

    public ResultadoEvaluacionCuestionarioBPDTO() {
    }

    public ResultadoEvaluacionCuestionarioBPDTO(
           String claveCIFIN,
           int codigoCuestionario,
           int numeroAciertos,
           RespuestaBPDTO respuestaProceso) {
           this.claveCIFIN = claveCIFIN;
           this.codigoCuestionario = codigoCuestionario;
           this.numeroAciertos = numeroAciertos;
           this.respuestaProceso = respuestaProceso;
    }


    /**
     * Gets the claveCIFIN value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @return claveCIFIN
     */
    public String getClaveCIFIN() {
        return claveCIFIN;
    }


    /**
     * Sets the claveCIFIN value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @param claveCIFIN
     */
    public void setClaveCIFIN(String claveCIFIN) {
        this.claveCIFIN = claveCIFIN;
    }


    /**
     * Gets the codigoCuestionario value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @return codigoCuestionario
     */
    public int getCodigoCuestionario() {
        return codigoCuestionario;
    }


    /**
     * Sets the codigoCuestionario value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @param codigoCuestionario
     */
    public void setCodigoCuestionario(int codigoCuestionario) {
        this.codigoCuestionario = codigoCuestionario;
    }


    /**
     * Gets the numeroAciertos value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @return numeroAciertos
     */
    public int getNumeroAciertos() {
        return numeroAciertos;
    }


    /**
     * Sets the numeroAciertos value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @param numeroAciertos
     */
    public void setNumeroAciertos(int numeroAciertos) {
        this.numeroAciertos = numeroAciertos;
    }


    /**
     * Gets the respuestaProceso value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @return respuestaProceso
     */
    public RespuestaBPDTO getRespuestaProceso() {
        return respuestaProceso;
    }


    /**
     * Sets the respuestaProceso value for this ResultadoEvaluacionCuestionarioBPDTO.
     * 
     * @param respuestaProceso
     */
    public void setRespuestaProceso(RespuestaBPDTO respuestaProceso) {
        this.respuestaProceso = respuestaProceso;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof ResultadoEvaluacionCuestionarioBPDTO)) return false;
        ResultadoEvaluacionCuestionarioBPDTO other = (ResultadoEvaluacionCuestionarioBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.claveCIFIN==null && other.getClaveCIFIN()==null) || 
             (this.claveCIFIN!=null &&
              this.claveCIFIN.equals(other.getClaveCIFIN()))) &&
            this.codigoCuestionario == other.getCodigoCuestionario() &&
            this.numeroAciertos == other.getNumeroAciertos() &&
            ((this.respuestaProceso==null && other.getRespuestaProceso()==null) || 
             (this.respuestaProceso!=null &&
              this.respuestaProceso.equals(other.getRespuestaProceso())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getClaveCIFIN() != null) {
            _hashCode += getClaveCIFIN().hashCode();
        }
        _hashCode += getCodigoCuestionario();
        _hashCode += getNumeroAciertos();
        if (getRespuestaProceso() != null) {
            _hashCode += getRespuestaProceso().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResultadoEvaluacionCuestionarioBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ResultadoEvaluacionCuestionarioBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("claveCIFIN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "claveCIFIN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numeroAciertos");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numeroAciertos"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("respuestaProceso");
        elemField.setXmlName(new javax.xml.namespace.QName("", "respuestaProceso"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaBPDTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

/**
 * ResultadoEvaluacionCuestionarioULTRADTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package co.cifin.confrontaultra.dto.ultra;

public class ResultadoEvaluacionCuestionarioULTRADTO  implements java.io.Serializable {
    private int numeroAciertos;

    private java.lang.String claveCIFIN;

    private int codigoCuestionario;

    private co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO respuestaProceso;

    private int resultadoConfrontacion;

    private int resultadoScore;

    public ResultadoEvaluacionCuestionarioULTRADTO() {
    }

    public ResultadoEvaluacionCuestionarioULTRADTO(
           int numeroAciertos,
           java.lang.String claveCIFIN,
           int codigoCuestionario,
           co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO respuestaProceso,
           int resultadoConfrontacion,
           int resultadoScore) {
           this.numeroAciertos = numeroAciertos;
           this.claveCIFIN = claveCIFIN;
           this.codigoCuestionario = codigoCuestionario;
           this.respuestaProceso = respuestaProceso;
           this.resultadoConfrontacion = resultadoConfrontacion;
           this.resultadoScore = resultadoScore;
    }


    /**
     * Gets the numeroAciertos value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @return numeroAciertos
     */
    public int getNumeroAciertos() {
        return numeroAciertos;
    }


    /**
     * Sets the numeroAciertos value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @param numeroAciertos
     */
    public void setNumeroAciertos(int numeroAciertos) {
        this.numeroAciertos = numeroAciertos;
    }


    /**
     * Gets the claveCIFIN value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @return claveCIFIN
     */
    public java.lang.String getClaveCIFIN() {
        return claveCIFIN;
    }


    /**
     * Sets the claveCIFIN value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @param claveCIFIN
     */
    public void setClaveCIFIN(java.lang.String claveCIFIN) {
        this.claveCIFIN = claveCIFIN;
    }


    /**
     * Gets the codigoCuestionario value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @return codigoCuestionario
     */
    public int getCodigoCuestionario() {
        return codigoCuestionario;
    }


    /**
     * Sets the codigoCuestionario value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @param codigoCuestionario
     */
    public void setCodigoCuestionario(int codigoCuestionario) {
        this.codigoCuestionario = codigoCuestionario;
    }


    /**
     * Gets the respuestaProceso value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @return respuestaProceso
     */
    public co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO getRespuestaProceso() {
        return respuestaProceso;
    }


    /**
     * Sets the respuestaProceso value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @param respuestaProceso
     */
    public void setRespuestaProceso(co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO respuestaProceso) {
        this.respuestaProceso = respuestaProceso;
    }


    /**
     * Gets the resultadoConfrontacion value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @return resultadoConfrontacion
     */
    public int getResultadoConfrontacion() {
        return resultadoConfrontacion;
    }


    /**
     * Sets the resultadoConfrontacion value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @param resultadoConfrontacion
     */
    public void setResultadoConfrontacion(int resultadoConfrontacion) {
        this.resultadoConfrontacion = resultadoConfrontacion;
    }


    /**
     * Gets the resultadoScore value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @return resultadoScore
     */
    public int getResultadoScore() {
        return resultadoScore;
    }


    /**
     * Sets the resultadoScore value for this ResultadoEvaluacionCuestionarioULTRADTO.
     * 
     * @param resultadoScore
     */
    public void setResultadoScore(int resultadoScore) {
        this.resultadoScore = resultadoScore;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResultadoEvaluacionCuestionarioULTRADTO)) return false;
        ResultadoEvaluacionCuestionarioULTRADTO other = (ResultadoEvaluacionCuestionarioULTRADTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.numeroAciertos == other.getNumeroAciertos() &&
            ((this.claveCIFIN==null && other.getClaveCIFIN()==null) || 
             (this.claveCIFIN!=null &&
              this.claveCIFIN.equals(other.getClaveCIFIN()))) &&
            this.codigoCuestionario == other.getCodigoCuestionario() &&
            ((this.respuestaProceso==null && other.getRespuestaProceso()==null) || 
             (this.respuestaProceso!=null &&
              this.respuestaProceso.equals(other.getRespuestaProceso()))) &&
            this.resultadoConfrontacion == other.getResultadoConfrontacion() &&
            this.resultadoScore == other.getResultadoScore();
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
        _hashCode += getNumeroAciertos();
        if (getClaveCIFIN() != null) {
            _hashCode += getClaveCIFIN().hashCode();
        }
        _hashCode += getCodigoCuestionario();
        if (getRespuestaProceso() != null) {
            _hashCode += getRespuestaProceso().hashCode();
        }
        _hashCode += getResultadoConfrontacion();
        _hashCode += getResultadoScore();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResultadoEvaluacionCuestionarioULTRADTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ResultadoEvaluacionCuestionarioULTRADTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numeroAciertos");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numeroAciertos"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
        elemField.setFieldName("respuestaProceso");
        elemField.setXmlName(new javax.xml.namespace.QName("", "respuestaProceso"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "RespuestaULTRADTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultadoConfrontacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resultadoConfrontacion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultadoScore");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resultadoScore"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
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
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

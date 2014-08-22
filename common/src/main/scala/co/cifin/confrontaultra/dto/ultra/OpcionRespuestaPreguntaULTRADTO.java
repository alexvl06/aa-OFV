/**
 * OpcionRespuestaPreguntaULTRADTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package co.cifin.confrontaultra.dto.ultra;

public class OpcionRespuestaPreguntaULTRADTO  extends co.cifin.confrontaultra.dto.ultra.RespuestaPreguntaULTRADTO  implements java.io.Serializable {
    private java.lang.String textoRespuesta;

    public OpcionRespuestaPreguntaULTRADTO() {
    }

    public OpcionRespuestaPreguntaULTRADTO(
           int secuenciaPregunta,
           int secuenciaRespuesta,
           java.lang.String textoRespuesta) {
        super(
            secuenciaPregunta,
            secuenciaRespuesta);
        this.textoRespuesta = textoRespuesta;
    }


    /**
     * Gets the textoRespuesta value for this OpcionRespuestaPreguntaULTRADTO.
     * 
     * @return textoRespuesta
     */
    public java.lang.String getTextoRespuesta() {
        return textoRespuesta;
    }


    /**
     * Sets the textoRespuesta value for this OpcionRespuestaPreguntaULTRADTO.
     * 
     * @param textoRespuesta
     */
    public void setTextoRespuesta(java.lang.String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof OpcionRespuestaPreguntaULTRADTO)) return false;
        OpcionRespuestaPreguntaULTRADTO other = (OpcionRespuestaPreguntaULTRADTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.textoRespuesta==null && other.getTextoRespuesta()==null) || 
             (this.textoRespuesta!=null &&
              this.textoRespuesta.equals(other.getTextoRespuesta())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getTextoRespuesta() != null) {
            _hashCode += getTextoRespuesta().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(OpcionRespuestaPreguntaULTRADTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "OpcionRespuestaPreguntaULTRADTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("textoRespuesta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "textoRespuesta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
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

/**
 * PreguntaULTRADTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package co.cifin.confrontaultra.dto.ultra;

public class PreguntaULTRADTO  implements java.io.Serializable {
    private int secuenciaPregunta;

    private co.cifin.confrontaultra.dto.ultra.OpcionRespuestaPreguntaULTRADTO[] listadoRespuestas;

    private int posicionPregunta;

    private java.lang.String textoPregunta;

    public PreguntaULTRADTO() {
    }

    public PreguntaULTRADTO(
           int secuenciaPregunta,
           co.cifin.confrontaultra.dto.ultra.OpcionRespuestaPreguntaULTRADTO[] listadoRespuestas,
           int posicionPregunta,
           java.lang.String textoPregunta) {
           this.secuenciaPregunta = secuenciaPregunta;
           this.listadoRespuestas = listadoRespuestas;
           this.posicionPregunta = posicionPregunta;
           this.textoPregunta = textoPregunta;
    }


    /**
     * Gets the secuenciaPregunta value for this PreguntaULTRADTO.
     * 
     * @return secuenciaPregunta
     */
    public int getSecuenciaPregunta() {
        return secuenciaPregunta;
    }


    /**
     * Sets the secuenciaPregunta value for this PreguntaULTRADTO.
     * 
     * @param secuenciaPregunta
     */
    public void setSecuenciaPregunta(int secuenciaPregunta) {
        this.secuenciaPregunta = secuenciaPregunta;
    }


    /**
     * Gets the listadoRespuestas value for this PreguntaULTRADTO.
     * 
     * @return listadoRespuestas
     */
    public co.cifin.confrontaultra.dto.ultra.OpcionRespuestaPreguntaULTRADTO[] getListadoRespuestas() {
        return listadoRespuestas;
    }


    /**
     * Sets the listadoRespuestas value for this PreguntaULTRADTO.
     * 
     * @param listadoRespuestas
     */
    public void setListadoRespuestas(co.cifin.confrontaultra.dto.ultra.OpcionRespuestaPreguntaULTRADTO[] listadoRespuestas) {
        this.listadoRespuestas = listadoRespuestas;
    }


    /**
     * Gets the posicionPregunta value for this PreguntaULTRADTO.
     * 
     * @return posicionPregunta
     */
    public int getPosicionPregunta() {
        return posicionPregunta;
    }


    /**
     * Sets the posicionPregunta value for this PreguntaULTRADTO.
     * 
     * @param posicionPregunta
     */
    public void setPosicionPregunta(int posicionPregunta) {
        this.posicionPregunta = posicionPregunta;
    }


    /**
     * Gets the textoPregunta value for this PreguntaULTRADTO.
     * 
     * @return textoPregunta
     */
    public java.lang.String getTextoPregunta() {
        return textoPregunta;
    }


    /**
     * Sets the textoPregunta value for this PreguntaULTRADTO.
     * 
     * @param textoPregunta
     */
    public void setTextoPregunta(java.lang.String textoPregunta) {
        this.textoPregunta = textoPregunta;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PreguntaULTRADTO)) return false;
        PreguntaULTRADTO other = (PreguntaULTRADTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.secuenciaPregunta == other.getSecuenciaPregunta() &&
            ((this.listadoRespuestas==null && other.getListadoRespuestas()==null) || 
             (this.listadoRespuestas!=null &&
              java.util.Arrays.equals(this.listadoRespuestas, other.getListadoRespuestas()))) &&
            this.posicionPregunta == other.getPosicionPregunta() &&
            ((this.textoPregunta==null && other.getTextoPregunta()==null) || 
             (this.textoPregunta!=null &&
              this.textoPregunta.equals(other.getTextoPregunta())));
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
        _hashCode += getSecuenciaPregunta();
        if (getListadoRespuestas() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getListadoRespuestas());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getListadoRespuestas(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getPosicionPregunta();
        if (getTextoPregunta() != null) {
            _hashCode += getTextoPregunta().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PreguntaULTRADTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "PreguntaULTRADTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secuenciaPregunta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "secuenciaPregunta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listadoRespuestas");
        elemField.setXmlName(new javax.xml.namespace.QName("", "listadoRespuestas"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "OpcionRespuestaPreguntaULTRADTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("posicionPregunta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "posicionPregunta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("textoPregunta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "textoPregunta"));
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

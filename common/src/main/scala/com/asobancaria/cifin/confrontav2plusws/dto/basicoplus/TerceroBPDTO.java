/**
 * TerceroBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class TerceroBPDTO  implements java.io.Serializable {
    private String codigoTipoIdentificacion;

    private String estadoIdentificacion;

    private String nombreCompleto;

    private String numeroIdentificacion;

    public TerceroBPDTO() {
    }

    public TerceroBPDTO(
           String codigoTipoIdentificacion,
           String estadoIdentificacion,
           String nombreCompleto,
           String numeroIdentificacion) {
           this.codigoTipoIdentificacion = codigoTipoIdentificacion;
           this.estadoIdentificacion = estadoIdentificacion;
           this.nombreCompleto = nombreCompleto;
           this.numeroIdentificacion = numeroIdentificacion;
    }


    /**
     * Gets the codigoTipoIdentificacion value for this TerceroBPDTO.
     * 
     * @return codigoTipoIdentificacion
     */
    public String getCodigoTipoIdentificacion() {
        return codigoTipoIdentificacion;
    }


    /**
     * Sets the codigoTipoIdentificacion value for this TerceroBPDTO.
     * 
     * @param codigoTipoIdentificacion
     */
    public void setCodigoTipoIdentificacion(String codigoTipoIdentificacion) {
        this.codigoTipoIdentificacion = codigoTipoIdentificacion;
    }


    /**
     * Gets the estadoIdentificacion value for this TerceroBPDTO.
     * 
     * @return estadoIdentificacion
     */
    public String getEstadoIdentificacion() {
        return estadoIdentificacion;
    }


    /**
     * Sets the estadoIdentificacion value for this TerceroBPDTO.
     * 
     * @param estadoIdentificacion
     */
    public void setEstadoIdentificacion(String estadoIdentificacion) {
        this.estadoIdentificacion = estadoIdentificacion;
    }


    /**
     * Gets the nombreCompleto value for this TerceroBPDTO.
     * 
     * @return nombreCompleto
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }


    /**
     * Sets the nombreCompleto value for this TerceroBPDTO.
     * 
     * @param nombreCompleto
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }


    /**
     * Gets the numeroIdentificacion value for this TerceroBPDTO.
     * 
     * @return numeroIdentificacion
     */
    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }


    /**
     * Sets the numeroIdentificacion value for this TerceroBPDTO.
     * 
     * @param numeroIdentificacion
     */
    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof TerceroBPDTO)) return false;
        TerceroBPDTO other = (TerceroBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.codigoTipoIdentificacion==null && other.getCodigoTipoIdentificacion()==null) || 
             (this.codigoTipoIdentificacion!=null &&
              this.codigoTipoIdentificacion.equals(other.getCodigoTipoIdentificacion()))) &&
            ((this.estadoIdentificacion==null && other.getEstadoIdentificacion()==null) || 
             (this.estadoIdentificacion!=null &&
              this.estadoIdentificacion.equals(other.getEstadoIdentificacion()))) &&
            ((this.nombreCompleto==null && other.getNombreCompleto()==null) || 
             (this.nombreCompleto!=null &&
              this.nombreCompleto.equals(other.getNombreCompleto()))) &&
            ((this.numeroIdentificacion==null && other.getNumeroIdentificacion()==null) || 
             (this.numeroIdentificacion!=null &&
              this.numeroIdentificacion.equals(other.getNumeroIdentificacion())));
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
        if (getCodigoTipoIdentificacion() != null) {
            _hashCode += getCodigoTipoIdentificacion().hashCode();
        }
        if (getEstadoIdentificacion() != null) {
            _hashCode += getEstadoIdentificacion().hashCode();
        }
        if (getNombreCompleto() != null) {
            _hashCode += getNombreCompleto().hashCode();
        }
        if (getNumeroIdentificacion() != null) {
            _hashCode += getNumeroIdentificacion().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TerceroBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "TerceroBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoTipoIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoTipoIdentificacion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("estadoIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "estadoIdentificacion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nombreCompleto");
        elemField.setXmlName(new javax.xml.namespace.QName("", "nombreCompleto"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numeroIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numeroIdentificacion"));
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

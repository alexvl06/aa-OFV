/**
 * TerceroULTRADTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package co.cifin.confrontaultra.dto.ultra;

public class TerceroULTRADTO  implements java.io.Serializable {
    private java.lang.String numeroIdentificacion;

    private java.lang.String codigoTipoIdentificacion;

    private java.lang.String nombreCompleto;

    private java.lang.String estadoIdentificacion;

    public TerceroULTRADTO() {
    }

    public TerceroULTRADTO(
           java.lang.String numeroIdentificacion,
           java.lang.String codigoTipoIdentificacion,
           java.lang.String nombreCompleto,
           java.lang.String estadoIdentificacion) {
           this.numeroIdentificacion = numeroIdentificacion;
           this.codigoTipoIdentificacion = codigoTipoIdentificacion;
           this.nombreCompleto = nombreCompleto;
           this.estadoIdentificacion = estadoIdentificacion;
    }


    /**
     * Gets the numeroIdentificacion value for this TerceroULTRADTO.
     * 
     * @return numeroIdentificacion
     */
    public java.lang.String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }


    /**
     * Sets the numeroIdentificacion value for this TerceroULTRADTO.
     * 
     * @param numeroIdentificacion
     */
    public void setNumeroIdentificacion(java.lang.String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }


    /**
     * Gets the codigoTipoIdentificacion value for this TerceroULTRADTO.
     * 
     * @return codigoTipoIdentificacion
     */
    public java.lang.String getCodigoTipoIdentificacion() {
        return codigoTipoIdentificacion;
    }


    /**
     * Sets the codigoTipoIdentificacion value for this TerceroULTRADTO.
     * 
     * @param codigoTipoIdentificacion
     */
    public void setCodigoTipoIdentificacion(java.lang.String codigoTipoIdentificacion) {
        this.codigoTipoIdentificacion = codigoTipoIdentificacion;
    }


    /**
     * Gets the nombreCompleto value for this TerceroULTRADTO.
     * 
     * @return nombreCompleto
     */
    public java.lang.String getNombreCompleto() {
        return nombreCompleto;
    }


    /**
     * Sets the nombreCompleto value for this TerceroULTRADTO.
     * 
     * @param nombreCompleto
     */
    public void setNombreCompleto(java.lang.String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }


    /**
     * Gets the estadoIdentificacion value for this TerceroULTRADTO.
     * 
     * @return estadoIdentificacion
     */
    public java.lang.String getEstadoIdentificacion() {
        return estadoIdentificacion;
    }


    /**
     * Sets the estadoIdentificacion value for this TerceroULTRADTO.
     * 
     * @param estadoIdentificacion
     */
    public void setEstadoIdentificacion(java.lang.String estadoIdentificacion) {
        this.estadoIdentificacion = estadoIdentificacion;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TerceroULTRADTO)) return false;
        TerceroULTRADTO other = (TerceroULTRADTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.numeroIdentificacion==null && other.getNumeroIdentificacion()==null) || 
             (this.numeroIdentificacion!=null &&
              this.numeroIdentificacion.equals(other.getNumeroIdentificacion()))) &&
            ((this.codigoTipoIdentificacion==null && other.getCodigoTipoIdentificacion()==null) || 
             (this.codigoTipoIdentificacion!=null &&
              this.codigoTipoIdentificacion.equals(other.getCodigoTipoIdentificacion()))) &&
            ((this.nombreCompleto==null && other.getNombreCompleto()==null) || 
             (this.nombreCompleto!=null &&
              this.nombreCompleto.equals(other.getNombreCompleto()))) &&
            ((this.estadoIdentificacion==null && other.getEstadoIdentificacion()==null) || 
             (this.estadoIdentificacion!=null &&
              this.estadoIdentificacion.equals(other.getEstadoIdentificacion())));
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
        if (getNumeroIdentificacion() != null) {
            _hashCode += getNumeroIdentificacion().hashCode();
        }
        if (getCodigoTipoIdentificacion() != null) {
            _hashCode += getCodigoTipoIdentificacion().hashCode();
        }
        if (getNombreCompleto() != null) {
            _hashCode += getNombreCompleto().hashCode();
        }
        if (getEstadoIdentificacion() != null) {
            _hashCode += getEstadoIdentificacion().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TerceroULTRADTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "TerceroULTRADTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numeroIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numeroIdentificacion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoTipoIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoTipoIdentificacion"));
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
        elemField.setFieldName("estadoIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "estadoIdentificacion"));
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

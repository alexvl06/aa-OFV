/**
 * ParametrosSeguridadULTRADTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package co.cifin.confrontaultra.dto.ultra;

public class ParametrosSeguridadULTRADTO  implements java.io.Serializable {
    private java.lang.String claveCIFIN;

    private java.lang.String password;

    public ParametrosSeguridadULTRADTO() {
    }

    public ParametrosSeguridadULTRADTO(
           java.lang.String claveCIFIN,
           java.lang.String password) {
           this.claveCIFIN = claveCIFIN;
           this.password = password;
    }


    /**
     * Gets the claveCIFIN value for this ParametrosSeguridadULTRADTO.
     * 
     * @return claveCIFIN
     */
    public java.lang.String getClaveCIFIN() {
        return claveCIFIN;
    }


    /**
     * Sets the claveCIFIN value for this ParametrosSeguridadULTRADTO.
     * 
     * @param claveCIFIN
     */
    public void setClaveCIFIN(java.lang.String claveCIFIN) {
        this.claveCIFIN = claveCIFIN;
    }


    /**
     * Gets the password value for this ParametrosSeguridadULTRADTO.
     * 
     * @return password
     */
    public java.lang.String getPassword() {
        return password;
    }


    /**
     * Sets the password value for this ParametrosSeguridadULTRADTO.
     * 
     * @param password
     */
    public void setPassword(java.lang.String password) {
        this.password = password;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ParametrosSeguridadULTRADTO)) return false;
        ParametrosSeguridadULTRADTO other = (ParametrosSeguridadULTRADTO) obj;
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
            ((this.password==null && other.getPassword()==null) || 
             (this.password!=null &&
              this.password.equals(other.getPassword())));
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
        if (getPassword() != null) {
            _hashCode += getPassword().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ParametrosSeguridadULTRADTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosSeguridadULTRADTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("claveCIFIN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "claveCIFIN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("password");
        elemField.setXmlName(new javax.xml.namespace.QName("", "password"));
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

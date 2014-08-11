/**
 * ConfrontaUltraWebServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS;

public class ConfrontaUltraWebServiceServiceLocator extends org.apache.axis.client.Service implements com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebServiceService {

    public ConfrontaUltraWebServiceServiceLocator() {
    }


    public ConfrontaUltraWebServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ConfrontaUltraWebServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ConfrontaUltraWS
    private java.lang.String ConfrontaUltraWS_address = "https://tciweb1.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaUltraWS";

    public java.lang.String getConfrontaUltraWSAddress() {
        return ConfrontaUltraWS_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ConfrontaUltraWSWSDDServiceName = "ConfrontaUltraWS";

    public java.lang.String getConfrontaUltraWSWSDDServiceName() {
        return ConfrontaUltraWSWSDDServiceName;
    }

    public void setConfrontaUltraWSWSDDServiceName(java.lang.String name) {
        ConfrontaUltraWSWSDDServiceName = name;
    }

    public com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebService getConfrontaUltraWS() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ConfrontaUltraWS_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getConfrontaUltraWS(endpoint);
    }

    public com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebService getConfrontaUltraWS(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWSSoapBindingStub _stub = new com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWSSoapBindingStub(portAddress, this);
            _stub.setPortName(getConfrontaUltraWSWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setConfrontaUltraWSEndpointAddress(java.lang.String address) {
        ConfrontaUltraWS_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebService.class.isAssignableFrom(serviceEndpointInterface)) {
                com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWSSoapBindingStub _stub = new com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWSSoapBindingStub(new java.net.URL(ConfrontaUltraWS_address), this);
                _stub.setPortName(getConfrontaUltraWSWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ConfrontaUltraWS".equals(inputPortName)) {
            return getConfrontaUltraWS();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("https://tciweb1.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaUltraWS", "ConfrontaUltraWebServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("https://tciweb1.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaUltraWS", "ConfrontaUltraWS"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ConfrontaUltraWS".equals(portName)) {
            setConfrontaUltraWSEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

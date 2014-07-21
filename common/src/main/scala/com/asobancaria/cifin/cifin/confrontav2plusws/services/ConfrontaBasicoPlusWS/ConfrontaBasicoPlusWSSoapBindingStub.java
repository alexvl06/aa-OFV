/**
 * ConfrontaBasicoPlusWSSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.cifin.confrontav2plusws.services.ConfrontaBasicoPlusWS;

public class ConfrontaBasicoPlusWSSoapBindingStub extends org.apache.axis.client.Stub implements ConfrontaBasicoPlusWebService {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[3];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("obtenerCuestionarioAdicional");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametrosSeguridad"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosSeguridadBPDTO"), com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametros"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosAdicionalBPDTO"), com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosAdicionalBPDTO.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "CuestionarioBPDTO"));
        oper.setReturnClass(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "obtenerCuestionarioAdicionalReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("obtenerCuestionario");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametrosSeguridad"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosSeguridadBPDTO"), com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametros"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosBPDTO"), com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosBPDTO.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "CuestionarioBPDTO"));
        oper.setReturnClass(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "obtenerCuestionarioReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("evaluarCuestionario");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametrosSeguridad"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosSeguridadBPDTO"), com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_respuestaCuestionario"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaCuestionarioBPDTO"), com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.RespuestaCuestionarioBPDTO.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ResultadoEvaluacionCuestionarioBPDTO"));
        oper.setReturnClass(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ResultadoEvaluacionCuestionarioBPDTO.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "evaluarCuestionarioReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[2] = oper;

    }

    public ConfrontaBasicoPlusWSSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ConfrontaBasicoPlusWSSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public ConfrontaBasicoPlusWSSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "CuestionarioBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "DatosPlusBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.DatosPlusBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "HuellaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.HuellaBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "OpcionRespuestaPreguntaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.OpcionRespuestaPreguntaBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosAdicionalBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosAdicionalBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosSeguridadBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "PreguntaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.PreguntaBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.RespuestaBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaCuestionarioBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.RespuestaCuestionarioBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaPreguntaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.RespuestaPreguntaBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ResultadoEvaluacionCuestionarioBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ResultadoEvaluacionCuestionarioBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "TerceroBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.TerceroBPDTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://cifin.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaBasicoPlusWS", "ArrayOf_tns1_HuellaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.HuellaBPDTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "HuellaBPDTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://cifin.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaBasicoPlusWS", "ArrayOf_tns1_OpcionRespuestaPreguntaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.OpcionRespuestaPreguntaBPDTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "OpcionRespuestaPreguntaBPDTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://cifin.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaBasicoPlusWS", "ArrayOf_tns1_PreguntaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.PreguntaBPDTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "PreguntaBPDTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://cifin.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaBasicoPlusWS", "ArrayOf_tns1_RespuestaPreguntaBPDTO");
            cachedSerQNames.add(qName);
            cls = com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.RespuestaPreguntaBPDTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaPreguntaBPDTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        Class cls = (Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            Class sf = (Class)
                                 cachedSerFactories.get(i);
                            Class df = (Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO obtenerCuestionarioAdicional(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO p_parametrosSeguridad, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosAdicionalBPDTO p_parametros) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://ws.confrontav2plusws.cifin.asobancaria.com", "obtenerCuestionarioAdicional"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        Object _resp = _call.invoke(new Object[] {p_parametrosSeguridad, p_parametros});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO) _resp;
            } catch (Exception _exception) {
                return (com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO) org.apache.axis.utils.JavaUtils.convert(_resp, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO obtenerCuestionario(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO p_parametrosSeguridad, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosBPDTO p_parametros) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://ws.confrontav2plusws.cifin.asobancaria.com", "obtenerCuestionario"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        Object _resp = _call.invoke(new Object[] {p_parametrosSeguridad, p_parametros});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO) _resp;
            } catch (Exception _exception) {
                return (com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO) org.apache.axis.utils.JavaUtils.convert(_resp, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ResultadoEvaluacionCuestionarioBPDTO evaluarCuestionario(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO p_parametrosSeguridad, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.RespuestaCuestionarioBPDTO p_respuestaCuestionario) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://ws.confrontav2plusws.cifin.asobancaria.com", "evaluarCuestionario"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        Object _resp = _call.invoke(new Object[] {p_parametrosSeguridad, p_respuestaCuestionario});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ResultadoEvaluacionCuestionarioBPDTO) _resp;
            } catch (Exception _exception) {
                return (com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ResultadoEvaluacionCuestionarioBPDTO) org.apache.axis.utils.JavaUtils.convert(_resp, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ResultadoEvaluacionCuestionarioBPDTO.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}

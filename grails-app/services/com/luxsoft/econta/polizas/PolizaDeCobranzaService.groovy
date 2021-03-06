package com.luxsoft.econta.polizas

import grails.transaction.Transactional

import com.luxsoft.impapx.contabilidad.Poliza
import com.luxsoft.impapx.contabilidad.*
import com.luxsoft.impapx.cxc.CXCAplicacion
import com.luxsoft.impapx.cxc.CXCPago

import util.MonedaUtils
import util.Rounding


@Transactional
class PolizaDeCobranzaService extends ProcesadorService{

    def procesar(Poliza poliza){
        poliza.descripcion = "Poliza de cobranza "
        def dia = poliza.fecha

        def asiento='CXC COBRO'
            
        def pagos=CXCPago.findAll("from CXCPago p where date(p.fecha)=? ",[dia])
            	
    	pagos.each{ pago->
    		//Cargo al banco
    		def desc="${pago.cliente.nombre} ${pago.formaDePago.substring(0,2)} "
    		if(pago.moneda==MonedaUtils.DOLARES){
    			desc+=" $pago.total * $pago.tc"
    		}

    		poliza.addToPartidas(
    			cuenta:pago.cuenta.cuentaContable,
    			debe:pago.total.abs()*pago.tc,
    			haber:0.0,
    			asiento:asiento,
    			descripcion:desc,
    			referencia:"$pago.referenciaBancaria"
    			,fecha:poliza.fecha
    			,tipo:poliza.tipo
    			,entidad:'CXCPago'
    			,origen:pago.id)
    		
    		pago.aplicaciones.each{aplic->
    			
    			//Abono a clientes
    			def clave="105-"+aplic.abono.cliente.subCuentaOperativa
    			poliza.addToPartidas(
    				cuenta:CuentaContable.buscarPorClave(clave),
    				debe:0.0,
    				haber:aplic.total,
    				asiento:asiento,
    				descripcion:"Fac: ${aplic?.factura?.facturaFolio} ${aplic?.factura?.fecha?.text()}  ",
    				referencia:"$pago.referenciaBancaria"
    				,fecha:poliza.fecha
    				,tipo:poliza.tipo
    				,entidad:'CXCAplicacion'
    				,origen:aplic.id)
    		}
    		
    		//Abono al IVA por trasladar
    		poliza.addToPartidas(
    			cuenta:CuentaContable.buscarPorClave("208-0001"),
    			debe:0.0,
    			haber:pago.impuesto.abs()*pago.tc,
    			asiento:asiento,
    			//descripcion:"Cobro de facturas",
                descripcion:desc,
    			referencia:"$pago.referenciaBancaria"
    			,fecha:poliza.fecha
    			,tipo:poliza.tipo
    			,entidad:'CXCPago'
    			,origen:pago.id)
    		
    		pago.aplicaciones.each{aplic->
    			
    			//Cargo a IVA por trasladar
    			def importe=MonedaUtils.calcularImporteDelTotal(aplic.total)
    			def impuesto=Rounding.round(MonedaUtils.calcularImpuesto(importe),2)
    			
    			poliza.addToPartidas(
    				cuenta:CuentaContable.buscarPorClave("209-0001"),
    				debe:impuesto,
    				haber:0.0,
    				asiento:asiento,
    				//descripcion:"Fac: ${aplic?.factura?.facturaFolio} ${aplic?.factura?.cliente?.nombre} ${pago.tc>1?'T.C'+pago.tc:''} ",
    				descripcion:"Fac: ${aplic?.factura?.facturaFolio} ${aplic?.factura?.fecha?.text()}  ",
                    referencia:"$pago.referenciaBancaria"
    				,fecha:poliza.fecha
    				,tipo:poliza.tipo
    				,entidad:'CXCAplicacion'
    				,origen:aplic.id)
    		}
		}
    	cuadrar(poliza)
		depurar(poliza)
		save poliza
    }
    
}

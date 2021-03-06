package com.luxsoft.impapx.tesoreria

import util.MonedaUtils;

import com.luxsoft.impapx.cxp.Aplicacion
import com.luxsoft.impapx.cxp.Pago;

class PagoProveedorService {

    def registrarEgreso(PagoProveedor pago) {
		def requisicion=pago.requisicion
		
		if(pago.cuenta.moneda!=MonedaUtils.PESOS){
			//Localizar el tipo de cambio 
		}
		MovimientoDeCuenta egreso = new MovimientoDeCuenta(cuenta:pago.cuenta
			,fecha:pago.fecha
			,moneda:pago.cuenta.moneda
			,tc:pago.tipoDeCambio
			,importe:requisicion.total.abs()*-1.0
			,ingreso:false
			,tipo:requisicion.formaDePago
			,origen:'CXP'
			//,concepto:'PAGO_PROVEEDOR'
			,referenciaBancaria: pago.referencia
			,concepto:"Pago: $requisicion.aFavor  $requisicion.concepto" 
			,comentario:'PAGO REQUISICION:'+requisicion.id)
		if(pago.requisicion.proveedor.tipo == 'FLETES'){
			egreso.grupo = true
		}
		
		def cxp=new Pago(
			proveedor:requisicion.proveedor
			,fecha:pago.fecha
			,moneda:pago.cuenta.moneda
			,tc:pago.tipoDeCambio
			,importe:requisicion.importe
			,impuestos:requisicion.impuestos
			,total:requisicion.total	
			,comentario:'PAGO REQUISICION:'+requisicion.id
			,pagoProveedor:pago
			,aplicado:0.0)
		
		pago.requisicion.partidas.each{
			if(it.factura){
				def aplicacion=new Aplicacion(
					fecha:pago.fecha
					,factura:it.factura
					,total:it.total
					,importe:0.0
					,impuesto:0.0
					,impuestoTasa:0.0)
				
				cxp.addToAplicaciones(aplicacion)
			}
		}
		
		pago.egreso=egreso
		pago.pago=cxp
		return pago.save(failOnError:true)
    }

    def eliminar(PagoProveedor pago){
    	if(pago.egreso)
    		pago.egreso.delete flush:true
    	pago.delete flush:true
    }
}

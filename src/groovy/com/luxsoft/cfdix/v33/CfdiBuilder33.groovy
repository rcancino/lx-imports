package com.luxsoft.cfdix.v33

import org.apache.commons.logging.LogFactory
import org.bouncycastle.util.encoders.Base64

import com.luxsoft.impapx.Empresa
import com.luxsoft.impapx.Venta

import com.luxsoft.lx.utils.MonedaUtils
import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.ObjectFactory
import lx.cfdi.v33.Comprobante

// Catalogos
import lx.cfdi.v33.CUsoCFDI
import lx.cfdi.v33.CMetodoPago
import lx.cfdi.v33.CTipoDeComprobante
import lx.cfdi.v33.CMoneda
import lx.cfdi.v33.CTipoFactor

/**
 * TODO: Parametrizar el regimenFiscal de
 */
class CfdiBuilder33 {

	private static final log=LogFactory.getLog(this)

    private factory = new ObjectFactory();
	private Comprobante comprobante;
    private Empresa empresa
    private Venta venta;

    private BigDecimal totalImpuestosTrasladados

    def build(Venta venta, String serie = 'FAC'){
        buildComprobante(venta, serie)
        .buildEmisor()
        .buildReceptor()
        .buildFormaDePago()
        .buildConceptos()
        .buildImpuestos()
        .buildTotales()
        .buildCertificado()
        
        return comprobante
    }
    

	def buildComprobante(Venta venta, String serie){
		log.info("Generando CFDI 3.3 para venta: ${venta.id}")
        this.empresa = Empresa.first()
		this.comprobante = factory.createComprobante()
        this.venta = venta;
        
        comprobante.version = "3.3"
        comprobante.tipoDeComprobante = CTipoDeComprobante.I
        comprobante.serie = serie
        comprobante.folio = venta.id
        comprobante.setFecha(DateUtils.getCfdiDate(new Date()))
        comprobante.moneda = CMoneda.MXN
        comprobante.lugarExpedicion = empresa.direccion.codigoPostal
        return this
	}

    def buildEmisor(){
        /**** Emisor ****/
        Comprobante.Emisor emisor = factory.createComprobanteEmisor()
        emisor.rfc = empresa.rfc
        emisor.nombre = empresa.nombre
        emisor.regimenFiscal = empresa.regimenClaveSat ?:'601' 
        comprobante.emisor = emisor
        return this
    }

    def buildReceptor(){
        /** Receptor ***/
        Comprobante.Receptor receptor = factory.createComprobanteReceptor()
        receptor.rfc = venta.cliente.rfc
        receptor.nombre = venta.cliente.nombre
        receptor.usoCFDI = CUsoCFDI.G_01 // Adquisicion de mercancías
        comprobante.receptor = receptor
        return this
    }

    def buildFormaDePago(){
        comprobante.formaPago = '99'
        comprobante.condicionesDePago = 'Credito 30 días'
        comprobante.metodoPago = CMetodoPago.PPD
        return this
    }

    def buildConceptos(){
        /** Conceptos ***/
        this.totalImpuestosTrasladados = 0.0
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
        this.venta.partidas.each { det ->
            
            Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()
            concepto.with { 
                assert det.producto.productoSat, 
                "No hay una claveProdServ definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
                assert det.producto.unidad.claveUnidadSat, 
                "No hay una claveUnidadSat definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
                assert det.producto.unidad.unidadSat, 
                "No hay una unidadSat definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
                String desc = det.producto.descripcion
                claveProdServ = det.producto.productoSat.claveProdServ
                noIdentificacion = det.producto.clave
                cantidad = MonedaUtils.round(det.cantidad / det.producto.unidad.factor,3)
                claveUnidad = det.producto.claveUnidadSat
                unidad = det.producto.unidad.clave
                descripcion = desc
                valorUnitario = det.precio
                importe = det.importe
                // Impuestos del concepto
                concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
                concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()
                Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1 
                traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
                traslado1.base =  det.importe
                traslado1.impuesto = '002'
                traslado1.tipoFactor = CTipoFactor.TASA
                traslado1.tasaOCuota = '0.160000'
                traslado1.importe = MonedaUtils.round(det.importe * 0.16)
                this.totalImpuestosTrasladados += traslado1.importe
                concepto.impuestos.traslados.traslado.add(traslado1)
                conceptos.concepto.add(concepto)

                def pedimento=det.embarque?.pedimento
                if(pedimento){
                    Comprobante.Conceptos.Concepto.InformacionAduanera aduana = 
                        factory.createComprobanteConceptosConceptoInformacionAduanera()
                    aduana.numeroPedimento = pedimento.pedimento
                    concepto.informacionAduanera.add(aduana)
                }
                comprobante.conceptos = conceptos
            }
            
            
            
        }
        
        return this
    }

    def buildImpuestos(){
        /** Impuestos **/

        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos()
        //impuestos.setTotalImpuestosTrasladados(venta.impuestos)
        impuestos.setTotalImpuestosTrasladados(this.totalImpuestosTrasladados)
        

        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados()
        Comprobante.Impuestos.Traslados.Traslado traslado = factory.createComprobanteImpuestosTrasladosTraslado()
        traslado.impuesto = '002'
        traslado.tipoFactor = CTipoFactor.TASA
        traslado.tasaOCuota = '0.160000'
        //traslado.importe = venta.impuestos
        traslado.importe = this.totalImpuestosTrasladados
        traslados.traslado.add(traslado)
        impuestos.traslados = traslados
        comprobante.setImpuestos(impuestos)
        return this
    }

    def buildTotales(){
        comprobante.subTotal = venta.subtotal
        comprobante.total = venta.importe + this.totalImpuestosTrasladados
        return this
    }

    def buildCertificado(){
        comprobante.setNoCertificado(empresa.numeroDeCertificado)
        byte[] encodedCert=Base64.encode(empresa.getCertificado().getEncoded())
        comprobante.setCertificado(new String(encodedCert))
        return this

    }

    Comprobante getComprobante(){
        return this.comprobante
    }

    
	

}
package com.luxsoft.impapx



import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang.StringUtils

@Secured(["hasRole('COMPRAS')"])
@Transactional(readOnly = true)
class FacturaDeImportacionController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def reportService

    def index(Integer max) {
        def periodo=session.periodo
        def list=FacturaDeImportacion.findAll(
            "from FacturaDeImportacion f  where date(f.fecha) between ? and ? order by f.fecha desc",
            [periodo.fechaInicial,periodo.fechaFinal])
        [facturaDeImportacionInstanceList:list]
    }

    def show(FacturaDeImportacion facturaDeImportacionInstance) {
        respond facturaDeImportacionInstance
    }

    def create() {
        respond new FacturaDeImportacion(fecha:new Date(),vencimiento:new Date()+1)
    }

    @Transactional
    def save(FacturaDeImportacion facturaDeImportacionInstance) {
        if (facturaDeImportacionInstance == null) {
            notFound()
            return
        }

        if (facturaDeImportacionInstance.hasErrors()) {
            respond facturaDeImportacionInstance.errors, view:'create'
            return
        }

        facturaDeImportacionInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'facturaDeImportacion.label', default: 'FacturaDeImportacion'), facturaDeImportacionInstance.id])
                redirect facturaDeImportacionInstance
            }
            '*' { respond facturaDeImportacionInstance, [status: CREATED] }
        }
    }

    def edit(FacturaDeImportacion facturaDeImportacionInstance) {
        respond facturaDeImportacionInstance
    }

    @Transactional
    def update(FacturaDeImportacion facturaDeImportacionInstance) {
        if (facturaDeImportacionInstance == null) {
            notFound()
            return
        }

        if (facturaDeImportacionInstance.hasErrors()) {
            respond facturaDeImportacionInstance.errors, view:'edit'
            return
        }

        facturaDeImportacionInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'FacturaDeImportacion.label', default: 'FacturaDeImportacion'), facturaDeImportacionInstance.id])
                redirect facturaDeImportacionInstance
            }
            '*'{ respond facturaDeImportacionInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(FacturaDeImportacion facturaDeImportacionInstance) {

        if (facturaDeImportacionInstance == null) {
            notFound()
            return
        }

        facturaDeImportacionInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'FacturaDeImportacion.label', default: 'FacturaDeImportacion'), facturaDeImportacionInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'facturaDeImportacion.label', default: 'FacturaDeImportacion'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }

    def programacionDePagos(){
    	
    	params.max = Math.min(params.max ? params.int('max') : 200, 1000)
    	params.sort='id'
    	params.order= "desc"
    	if(!params.fechaInicial)
    		params.fechaInicial=new Date()
    	if(!params.fechaFinal)
    		params.fechaFinal=new Date()+7
    	
    	// FacturasPorPeriodoCommand cmd=new FacturasPorPeriodoCommand()
    	// cmd.properties=params
    	// println 'Periodo: '+cmd+' Proveedor: '+cmd?.proveedor?.id
    	
    	def facturas=[]
    	if(StringUtils.isNotBlank(params.proveedor)){
    		Proveedor p=Proveedor.get(params.long('proveedor.id'))
    		cmd.proveedor=p
    		facturas=FacturaDeImportacion.findAllByProveedorAndVencimientoBetween(p,params.fechaInicial,params.fechaFinal,params)
    	}
    	else{
    		facturas=FacturaDeImportacion.findAllByVencimientoBetween(params.fechaInicial,params.fechaFinal,params)
    	}
    	flash.message='Facturas: '+facturas.size()
    	[facturaDeImportacionInstanceList: facturas,facturaDeImportacionInstanceCount: facturas.size()]
    }

    def imprimirProgramacionDePagos(){
    	if(!params.fechaInicial)
    		params.fechaInicial=new Date()
    	if(!params.fechaFinal)
    		params.fechaFinal=new Date()+7

    	def command=new com.luxsoft.lx.bi.ReportCommand()
    	command.reportName="ProgramacionDePago"
    	command.empresa=session.empresa
    	def repParams=[
    		EMPRESA:session.empresa.nombre,
    	    FECHA_INI:params.fechaInicial.format('dd/MM/yyyy'),
    	    FECHA_FIN:params.fechaFinal.format('dd/MM/yyyy'),
    	    PROVEEDOR:'%'
    	]
    	def stream=reportService.build(command,repParams)
    	def file="ProgramacionDePagos_"+new Date().format('mmss')+'.'+command.formato.toLowerCase()
    	render(
    	    file: stream.toByteArray(), 
    	    contentType: 'application/pdf',
    	    fileName:file)
    }

    def search(){
        def term='%'+params.term.trim()+'%'
        def query=FacturaDeImportacion.where{
            //(id.toString()=~term || documento=~term || proveedor.nombre=~term || comentario=~term || pedimento?.pedimento=~term) 
            (id.toString()=~term || documento=~term || proveedor.nombre=~term ) 
        }
        def cuentas=query.list(max:30, sort:"id",order:'desc')

        def cuentasList=cuentas.collect { cuenta ->
            def label="Id: ${cuenta.id} Docto:${cuenta.documento} ${cuenta.proveedor} ${cuenta.fecha.format('dd/MM/yyyy')} ${cuenta.total} ${cuenta?.pedimento?.pedimento?:''}"
            [id:cuenta.id,label:label,value:label]
        }
        render cuentasList as JSON
    }
}

class FacturasPorPeriodoCommand{
	
	Date fechaInicial=new Date()-90
	Date fechaFinal=new Date()
	Proveedor proveedor
	
	String toString(){
		return fechaInicial.text() +' al '+fechaFinal.text() 
	}
	
}

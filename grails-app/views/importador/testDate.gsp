<html>
  <head>
    <title>Simple GSP page</title>
    <g:javascript library="jquery" />
    <r:require module="jquery-ui"/>
    <r:layoutResources />
    <!-- <jqui:resources theme="darkness" /> -->
    <script type="text/javascript">
        $(document).ready(function()
        {
          $("#datepicker").datepicker({dateFormat: 'yy/mm/dd'});
        })
    </script>

  </head>
  <body>
    <div>
      <p> Between <input type="text" id="datepicker"> </p>        
    </div>

  </body>
</html>
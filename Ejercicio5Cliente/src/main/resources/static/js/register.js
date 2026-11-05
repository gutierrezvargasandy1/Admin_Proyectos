$(document).ready(function(){
 	
		'use strict'
	
	  // Fetch all the forms we want to apply custom Bootstrap validation styles to
	  const forms = document.querySelectorAll('.needs-validation')
	
	  // Loop over them and prevent submission
	  Array.from(forms).forEach(form => {
	    form.addEventListener('submit', event => {
	      if (!form.checkValidity()) {
	        event.preventDefault()
	        event.stopPropagation()
	      }
	
	      form.classList.add('was-validated')
	    }, false)
	  })
});

function getRandomColor(){
	// Color de avatar aleatorio. Se usa crypto.getRandomValues (generador seguro) en lugar de Math.random.
	const valores = new Uint8Array(3);
	crypto.getRandomValues(valores);
	const color = '#' + Array.from(valores, v => v.toString(16).padStart(2, '0')).join('').toUpperCase();

	document.getElementById("imagenUsuario").value = color;
}

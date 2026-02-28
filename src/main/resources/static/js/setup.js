
        document.getElementById("setupForm").addEventListener("submit", function(event) {
            var pass = document.getElementById("password").value;
            var confirmPass = document.getElementById("confirmPassword").value;
            
            if (pass !== confirmPass) {
                event.preventDefault(); 
                document.getElementById("matchError").style.display = "block";
            } else {
                document.getElementById("matchError").style.display = "none";
            }
        });

function validatePasswordForm() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    if (newPassword !== confirmPassword) {
        alert('New passwords do not match!');
        return false;
    }
    
    if (newPassword.length < 6) {
        alert('Password must be at least 6 characters!');
        return false;
    }
    
    return true;
}
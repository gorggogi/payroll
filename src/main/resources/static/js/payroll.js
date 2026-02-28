
// set default period from empPayType if available
document.addEventListener('DOMContentLoaded', function(){
    try {
        if (typeof empPayType !== 'undefined' && empPayType) {
            var v = empPayType.toString().toLowerCase();
            if (v === 'monthly' || v === 'biweekly') {
                var sel = document.getElementById('period');
                if (sel) sel.value = v;
            }
        }
    } catch(e) { /* ignore */ }
});

document.getElementById('computePayrollBtn').addEventListener('click', function() {
            if (!empId || empId === 0) {
                alert('Employee id not available. Are you logged in?');
                return;
            }
            var period = document.getElementById('period').value;
            var month = document.getElementById('month').value;
            var url = `/api/payroll/${empId}?period=${encodeURIComponent(period)}&month=${encodeURIComponent(month)}`;

            fetch(url)
                .then(function(resp){
                    if (!resp.ok) {
                        if (resp.status === 403) {
                            throw new Error('Access denied to this payroll.');
                        }
                        if (resp.status === 404) {
                            throw new Error('No payroll data found for selected period.');
                        }
                        throw new Error('Server error (' + resp.status + ').');
                    }
                    return resp.json();
                })
                .then(function(data){
                    var container = document.getElementById('payrollResult');
                    if (!data || data.length === 0) {
                        container.innerHTML = '<p>No payroll records found for selected period.</p>';
                        return;
                    }
                    var html = '<table class="attendanceTable"><tr><th>Payroll Item</th><th>Basic</th><th>Gross</th><th>Total Deductions</th><th>Net Pay</th></tr>';
                    data.forEach(function(item){
                        html += `<tr><td>${item.payrollItemId || ''}</td><td>${item.basicPay || ''}</td><td>${item.grossPay || ''}</td><td>${item.totalDeductions || ''}</td><td>${item.netPay || ''}</td></tr>`;
                    });
                    html += '</table>';
                    container.innerHTML = html;
                })
                .catch(function(err){
                    document.getElementById('payrollResult').innerHTML = '<p>Error fetching payroll: ' + err.message + '</p>';
                });
        });


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
            var yearEl = document.getElementById('year');
            var year = yearEl ? yearEl.value : '';
            var url = `/api/payroll/${empId}?period=${encodeURIComponent(period)}&month=${encodeURIComponent(month)}&year=${encodeURIComponent(year)}`;

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
                    var html = '';
                    data.forEach(function(item){
                        function fmt(val) {
                            return (val !== null && val !== undefined) ? Number(val).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2}) : '0.00';
                        }
                        
                        html += '<table class="attendanceTable payslip-table" style="margin-bottom: 2rem;">';
                        html += '<caption>Payslip (Item ID: ' + (item.payrollItemId || '') + ')</caption>';
                        html += '<thead><tr class="attendanceHeader"><th>Earnings / Deductions</th><th>Amount (PHP)</th></tr></thead>';
                        html += '<tbody class="attendanceBody">';
                        
                        html += '<tr><td colspan="2" class="table-section-header"><strong>Rates</strong></td></tr>';
                        html += `<tr><td>Daily Rate</td><td>${fmt(item.dailyRate)}</td></tr>`;
                        html += `<tr><td>Hourly Rate</td><td>${fmt(item.hourlyRate)}</td></tr>`;
                        
                        html += '<tr><td colspan="2" class="table-section-header"><strong>Earnings</strong></td></tr>';
                        html += `<tr><td>Basic Pay <span class="rate-note">${item.totalWorkedHours != null ? '(' + item.totalWorkedHours + ' hrs)' : ''}</span></td><td>${fmt(item.basicPay)}</td></tr>`;
                        html += `<tr><td>Overtime Pay <span class="rate-note">${item.totalOtHours != null ? '(' + item.totalOtHours + ' hrs)' : ''}</span></td><td>${fmt(item.overtimePay)}</td></tr>`;
                        html += `<tr><td>Holiday Pay</td><td>${fmt(item.holidayPay)}</td></tr>`;
                        html += `<tr><td>Allowances</td><td>${fmt(item.allowances)}</td></tr>`;
                        html += `<tr><td>Adjustment (Earnings)</td><td>${fmt(item.adjustmentEarnings)}</td></tr>`;
                        html += `<tr class="gross-row"><td><strong>Total Earnings / Gross Pay</strong></td><td>${fmt(item.totalEarnings != null ? item.totalEarnings : item.grossPay)}</td></tr>`;

                        html += '<tr><td colspan="2" class="table-section-header"><strong>Other Deductions & Adjustments</strong></td></tr>';
                        html += `<tr><td>Late/Undertime Deduction <span class="rate-note">${item.lateUndertimeMinutes != null ? '(' + item.lateUndertimeMinutes + ' mins)' : ''}</span></td><td>${fmt(item.lateUndertimeDeduction)}</td></tr>`;
                        html += `<tr><td>Employee Deductions</td><td>${fmt(item.adjustmentDeductions)}</td></tr>`;
                        html += `<tr class="deduct-row"><td><strong>Total Non-Statutory Deductions</strong></td><td>${fmt(item.otherDeductions)}</td></tr>`;
                        html += `<tr class="service-fee-row"><td><strong>Service Fee</strong></td><td>${fmt(item.serviceFee)}</td></tr>`;

                        html += '<tr><td colspan="2" class="table-section-header"><strong>Statutory Contributions</strong></td></tr>';
                        html += `<tr><td>SSS</td><td>${fmt(item.sss)}</td></tr>`;
                        html += `<tr><td>Philhealth</td><td>${fmt(item.philhealth)}</td></tr>`;
                        html += `<tr><td>Pag-ibig</td><td>${fmt(item.pagibig)}</td></tr>`;
                        html += `<tr><td>Withholding Tax</td><td>${fmt(item.tax)}</td></tr>`;
                        
                        if (item.semiMonthlyContributions != null) {
                            html += `<tr class="deduct-row"><td><strong>Total Semi-monthly Contributions</strong></td><td>${fmt(item.semiMonthlyContributions)}</td></tr>`;
                        } else {
                            html += `<tr class="deduct-row"><td><strong>Total Deductions</strong></td><td>${fmt(item.totalDeductions)}</td></tr>`;
                        }
                        
                        html += `<tr class="net-row"><td><strong>Net Pay</strong></td><td>${fmt(item.netPay)}</td></tr>`;
                        
                        html += '</tbody></table>';
                    });
                    container.innerHTML = html;
                })
                .catch(function(err){
                    document.getElementById('payrollResult').innerHTML = '<p>Error fetching payroll: ' + err.message + '</p>';
                });
        });

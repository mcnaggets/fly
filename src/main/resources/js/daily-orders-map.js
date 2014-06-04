function dailyOrdersMapFunction() {
    var key = new Date(this.deadLine.getFullYear(), this.deadLine.getMonth(), this.deadLine.getDate());
    emit(key, {
        price : this.price,
        readyCount: +(this.status == 'READY'),
        paidCount: +(this.status == 'PAID')
    });
}
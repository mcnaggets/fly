function dailyOrdersReduceFunction(key, values) {
    return values.reduce(function(prev, current) {
        if (current.status == 'READY') {
            prev.readyCount++;
        } else if (current.status == 'PAID') {
            prev.paidCount++;
        }
        prev.price += current.price;
        return prev;
    }, {
        price: 0,
        readyCount: 0,
        paidCount: 0
    });
}
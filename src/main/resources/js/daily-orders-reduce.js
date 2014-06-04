function dailyOrdersReduceFunction(key, values) {
    return values.reduce(function(accum, current) {
        accum.readyCount += current.readyCount;
        accum.paidCount += current.paidCount;
        accum.price += current.price;
        return accum;
    }, {
        price: 0,
        readyCount: 0,
        paidCount: 0
    });
}
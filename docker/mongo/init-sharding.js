// Підключення шардів до кластера та шардінг колекції books.
// Ключ шардування: hashed(_id) — рівномірний розподіл між трьома шардами
// (range-шардінг за жанром дав би перекіс: жанр Fiction набагато більший за інші).
const shards = [
  'shard1rs/shard1:27017',
  'shard2rs/shard2:27017',
  'shard3rs/shard3:27017'
];

shards.forEach(s => {
  let ok = false;
  for (let i = 0; i < 60 && !ok; i++) {
    try {
      ok = sh.addShard(s).ok === 1;
    } catch (e) {
      print('retry addShard ' + s + ': ' + e.message);
      sleep(3000);
    }
  }
  if (!ok) throw new Error('addShard failed: ' + s);
  print('shard added: ' + s);
});

sh.enableSharding('booksdb');

const res = db.adminCommand({
  shardCollection: 'booksdb.books',
  key: { _id: 'hashed' },
  numInitialChunks: 6
});
printjson(res);
print('Sharding of booksdb.books is configured.');

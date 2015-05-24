-- select the items of a player
SELECT *
FROM item
WHERE idplayer = :idplayer AND symbol = :symbol

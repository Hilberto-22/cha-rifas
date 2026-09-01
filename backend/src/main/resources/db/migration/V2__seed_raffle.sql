insert into raffles (id, title, draw_at, number_price, first_prize, second_prize, pix_key)
values ('9d325c50-d845-4bf3-85e7-1bf14e9ed928', 'Chá Rifa do José Lucca', '2026-10-03 22:30:00+00', 25.00, 250.00, 100.00, null);

insert into raffle_numbers (id, raffle_id, number, status)
select gen_random_uuid(), '9d325c50-d845-4bf3-85e7-1bf14e9ed928', value, 'AVAILABLE'
from generate_series(1, 100) as value;

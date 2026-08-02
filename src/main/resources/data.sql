INSERT INTO ms_user (user_name)
SELECT 'Alice'
WHERE NOT EXISTS (SELECT 1 FROM ms_user);

INSERT INTO ms_user (user_name)
SELECT 'Budi'
WHERE NOT EXISTS (SELECT 1 FROM ms_user WHERE user_name = 'Budi');

INSERT INTO ms_event (event_name, event_venue, event_time, sale_start, sale_end, ticket_total, ticket_available)
SELECT 'Java Rock Festival', 'Gelora Bung Karno, Jakarta',
       now() + interval '30 days', now() - interval '1 hour', now() + interval '7 days',
       10000, 10000
WHERE NOT EXISTS (SELECT 1 FROM ms_event);

INSERT INTO ms_event (event_name, event_venue, event_time, sale_start, sale_end, ticket_total, ticket_available)
SELECT 'Jazz Night', 'Sabuga, Bandung',
       now() + interval '45 days', now() + interval '1 day', now() + interval '2 days',
       500, 500
WHERE NOT EXISTS (SELECT 1 FROM ms_event WHERE event_name = 'Jazz Night');
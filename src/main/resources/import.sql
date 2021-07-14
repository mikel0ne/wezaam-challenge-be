INSERT INTO users VALUES (1, 'David', 100);
INSERT INTO users VALUES (2, 'Arnold', 200);
INSERT INTO users VALUES (3, 'Ilona', 150);

INSERT INTO payment_methods (id, user_id, name) VALUES (1, 1, 'My bank account');
INSERT INTO payment_methods (id, user_id, name) VALUES (2, 1, 'My mom account');
INSERT INTO payment_methods (id, user_id, name) VALUES (3, 2, 'Work account');
INSERT INTO payment_methods (id, user_id, name) VALUES (4, 3, 'My bank account');
INSERT INTO payment_methods (id, user_id, name) VALUES (5, 3, 'Secret account');

INSERT INTO withdrawals (id, transaction_id, amount, created_at, user_id, payment_method_id, status) VALUES (1, 1, 30, NOW(), 1, select min(id) from payment_methods where user_id=1, 'SUCCESS');
INSERT INTO withdrawals (id, transaction_id, amount, created_at, user_id, payment_method_id, status) VALUES (2, 2, 180, NOW(), 2, select min(id) from payment_methods where user_id=2, 'INTERNAL_ERROR');
INSERT INTO scheduled_withdrawals (id, transaction_id, amount, created_at, execute_at, user_id, payment_method_id, status) VALUES (3, 3, 150, NOW(), NOW(), 3, select min(id) from payment_methods where user_id=3, 'PROCESSING');

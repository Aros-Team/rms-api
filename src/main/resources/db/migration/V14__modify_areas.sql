/*
| add new column "for_user" (used to indicate that is planed for assign on users)
| add new column "for_product" (used to indicate that is planed for assign on product)
*/
ALTER TABLE areas
ADD COLUMN for_user BOOLEAN NOT NULL DEFAULT 0,
ADD COLUMN for_product BOOLEAN NOT NULL DEFAULT 1;

/*
| add default register for cashier users to use
*/
INSERT INTO areas (name, type, enabled, for_user, for_product) VALUES
("Caja", "CASH", FALSE, TRUE, FALSE);
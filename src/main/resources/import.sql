-- You can use this file to load seed data into the database using SQL statements
insert into Member (id, name, email, phone_number) values (0, 'John Smith', 'john.smith@mailinator.com', '2125551212')

insert into Principals(PrincipalID, Password, address1, city, state, zip, country, email, firstName, lastName, activated) values ("p", "22101006ac955303fe9b999cf2df97b3", "adline1","nowhere", "nostate", "99999", "USA","p@sigh.com", "peekay", "Singh", true)
insert into Principals(PrincipalID, Password, address1, city, state, zip, country, email, firstName, lastName, activated) values ("k", "dc1d310f0607435881918faeddc2190e", "adline1","nowhere", "nostate", "99999", "USA","k@sigh.com", "kay", "Singh", true)
--insert into Principals(PrincipalID, Password, address1, city, state, zip, country, email, firstName, lastName, activated) values ("baddu@user.com", "71037e01e44c2ab81c1dfbda2f91082c", "adline1","nowhere", "nostate", "99999", "USA","baddu@user.com", "bkay", "Singh", true)



insert into Roles(PrincipalID, Role, RoleGroup, roleOrder) values ("p", "user", "Roles",1)
insert into Roles(PrincipalID, Role, RoleGroup, roleOrder) values ("k", "user", "Roles",1)
insert into Roles(PrincipalID, Role, RoleGroup, roleOrder) values ("k", "admin", "Roles",2)
--insert into Roles(PrincipalID, Role, RoleGroup, roleOrder) values ("baddu@user.com", "user", "Roles",1)


insert into RentalUser(PrincipalID, userId) values ("p", "p")
insert into RentalUser(PrincipalID, userId) values ("k", "k")
insert into UserPhone(userName, phoneType, phoneNumber) values ("p", 'HOME', '111-111-1111')
insert into UserPhone(userName, phoneType, phoneNumber) values ("p", 'MOBILE', '222-222-2222')
insert into UserPhone(userName, phoneType, phoneNumber) values ("k", 'HOME', '111-111-1111')
insert into UserPhone(userName, phoneType, phoneNumber) values ("k", 'MOBILE', '333-333-3333')

insert into RentalItem(id, name, itemCategory, itemSubCategory, itemBrand, consolidatedRating, enableRenting, itemAge,pastRentalCount,replacementCost, currentlyRented, ownerId) values (3, 'Screw Driver', 'Wood', 'Tool', 'Bosch', 0 ,true,1,0,0,false, "p")
insert into RentalItem(id, name, itemCategory, itemSubCategory, itemBrand, consolidatedRating, enableRenting, itemAge,pastRentalCount,replacementCost, currentlyRented, ownerId) values (4, 'Plier', 'Metal', 'Tool','Bosch',0 ,true,1,0,0, false, "p")
insert into RentalItem(id, name, itemCategory, itemSubCategory, itemBrand, consolidatedRating, enableRenting, itemAge,pastRentalCount,replacementCost, currentlyRented, ownerId) values (5, 'Hammer', 'Metal', 'Tool','Bosch',0 ,true,1,0,0, false, "k")
insert into RentalItem(id, name, itemCategory, itemSubCategory, itemBrand, consolidatedRating, enableRenting, itemAge,pastRentalCount,replacementCost, currentlyRented, ownerId) values (6, 'Cutter', 'Metal', 'Tool','Black and Decker', 0 ,false,1,0,0, false, "k")
insert into RentalPenaltyTerm(rentalItemId, penaltyTermOrder, penaltyCharge, penaltyTerm) values (3, 1, 10.0,'misuse')
insert into RentalPenaltyTerm(rentalItemId, penaltyTermOrder, penaltyCharge, penaltyTerm) values (4, 1, 30.0,'bend')
insert into RentalLocation(rentalItemId, locationOrder, contactName, contactPhoneNumber, address1, city, state, zip, country, email) values (3, 1, "SD Owner", "999-999-9999", "SD Add","nowhere", "nostate", "99999", "USA","p@sigh.com")
insert into RentalLocation(rentalItemId, locationOrder, contactName, contactPhoneNumber, address1, city, state, zip, country, email) values (4, 1, "Plier Owner", "999-999-9999", "Pl Add","nowhere", "nostate", "99999", "USA","p@sigh.com")
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (3, 1,'DAY',10.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (3, 2,'WEEK',70.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (4, 1,'DAY',10.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (4, 2,'WEEK',70.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (4, 3,'MONTH',100.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (5, 1,'DAY',10.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (5, 2,'WEEK',70.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (6, 1,'HOUR',10.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (6, 2,'MONTH',70.0,10.0,true)
insert into RentalItemCost(rentalItemId, costOrder, period, cost, safetyDeposit, active) values (6, 3,'WEEK',30.0,10.0,true)

--insert into OwnerItem(ownerId,OwnedItemId) values ("p@sigh.com",1)
--insert into OwnerItem(ownerId,OwnedItemId) values ("p@sigh.com",2)
--insert into OwnerItem(ownerId,OwnedItemId) values ("k@sigh.com",3)
--insert into OwnerItem(ownerId,OwnedItemId) values ("k@sigh.com",4)
update hibernate_sequence set next_val = 20
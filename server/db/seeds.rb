# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the rake db:seed (or created alongside the db with db:setup).
#
# Examples:
#
#   cities = City.create([{ name: 'Chicago' }, { name: 'Copenhagen' }])
#   Mayor.create(name: 'Emanuel', city: cities.first)

Event.create(title: "Prototype Launch 1", admin: 1)
Event.create(title: "Prototype Launch 2", admin: 2)

User.create(first_name: "Salman Tariq", last_name: "Mirza", email: "stariqmi@gmail.com", event_id: 1, event_wishlist: "1,2")
User.create(first_name: "test user 1", last_name: "test last name", email: "test@gmail.com")

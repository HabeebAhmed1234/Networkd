# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20140313071656) do

  create_table "contacts", force: true do |t|
    t.integer  "user_id"
    t.text     "summary"
    t.text     "skills"
    t.text     "extra_notes"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "sender_id"
  end

  add_index "contacts", ["sender_id"], name: "index_contacts_on_sender_id"
  add_index "contacts", ["user_id"], name: "index_contacts_on_user_id"

  create_table "conversation_managers", force: true do |t|
    t.integer  "user_id"
    t.integer  "conversation_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "conversation_managers", ["conversation_id"], name: "index_conversation_managers_on_conversation_id"
  add_index "conversation_managers", ["user_id"], name: "index_conversation_managers_on_user_id"

  create_table "conversations", force: true do |t|
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "events", force: true do |t|
    t.string   "title"
    t.integer  "admin"
    t.date     "start_date"
    t.date     "end_date"
    t.string   "start_time"
    t.string   "end_time"
    t.string   "address"
    t.string   "gps_coord"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "events", ["title"], name: "index_events_on_title", unique: true

  create_table "messages", force: true do |t|
    t.integer  "conversation_id"
    t.integer  "user_id"
    t.text     "message"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "messages", ["conversation_id"], name: "index_messages_on_conversation_id"
  add_index "messages", ["user_id"], name: "index_messages_on_user_id"

  create_table "notes", force: true do |t|
    t.integer  "user_id"
    t.string   "linkedin_id"
    t.text     "note"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "notes", ["user_id"], name: "index_notes_on_user_id"

  create_table "shortlists", force: true do |t|
    t.integer  "user_id"
    t.integer  "follow_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "users", force: true do |t|
    t.string   "first_name"
    t.string   "last_name"
    t.string   "linkedin_id"
    t.string   "email",          null: false
    t.string   "gps_coord"
    t.integer  "event_id"
    t.string   "event_wishlist"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "api_key"
    t.integer  "attend"
    t.string   "linkedin_key"
  end

  add_index "users", ["api_key"], name: "index_users_on_api_key", unique: true
  add_index "users", ["email"], name: "index_users_on_email", unique: true

  create_table "wishes", force: true do |t|
    t.integer  "user_id"
    t.integer  "event_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end

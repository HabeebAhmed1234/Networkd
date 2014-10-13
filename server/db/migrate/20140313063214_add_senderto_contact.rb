class AddSendertoContact < ActiveRecord::Migration
  def change
  	add_column :contacts, :sender_id, :integer
  	add_index :contacts, :sender_id
  end
end

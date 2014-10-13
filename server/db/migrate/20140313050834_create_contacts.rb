class CreateContacts < ActiveRecord::Migration
  def change
    create_table :contacts do |t|
      t.integer :user_id
      t.string :first_name
      t.string :last_name
      t.string :linkedin_id
      t.text :summary
      t.text :skills
      t.text :extra_notes

      t.timestamps
    end
  end
end

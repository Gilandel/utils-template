-- Get the name and height of all motorcycles or bikes, following if they contain an engine, with a length inferior to 1500mm
-- if it's a racing or touring motorbike, get the tire front and rear width, 
-- if it's a racing bike, get the tire width,
-- else return -1
select
		b.name,
		s.height,
		{engine && (racing || touring)??
			tf.width as front_tire,
			tr.width as rear_tire
		::
			{!engine && racing??
				t.width,
				t.width
			::
				-1,
				-1
			}
		}
	from
	{engine??
		motorcycle b
		inner join specification s on b.id=s.fk_vehicule
		{racing || touring??
			inner join tire tf on tf.id=s.fk_front_tire
			inner join tire tr on tr.id=s.fk_rear_tire
		}
	::
		bike b
		inner join specification s on b.id=s.fk_vehicule
		{racing??
			inner join tire t on t.id=s.fk_tire
		}
	}
	where s.length < 1500;